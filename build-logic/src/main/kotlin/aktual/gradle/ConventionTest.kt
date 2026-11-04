@file:Suppress("MagicNumber")

package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import app.cash.burst.gradle.BurstPlugin
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigPlugin
import java.io.File
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.gradle.testretry.TestRetryPlugin
import org.gradle.testretry.TestRetryTaskExtension

class ConventionTest : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(ConventionTestDependencies::class)
      apply(BuildConfigPlugin::class)
      apply(TestRetryPlugin::class)
    }

    val isCi = providers.environmentVariable("CI").isPresent

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
      pluginManager.apply(BurstPlugin::class)
    }

    extensions.configure(BuildConfigExtension::class) {
      sourceSets.named("test") { ss ->
        ss.forClass("Resources") { spec ->
          spec.packageName("aktual.test")
          spec.useKotlinOutput { o -> o.topLevelConstants = true }
          spec.buildConfigField(
            File::class.java,
            "RESOURCES_DIR",
            layout.projectDirectory.dir("src/commonTest/resources").asFile,
          )
        }
      }
    }

    val testTasks = tasks.withType(Test::class)

    tasks.register("testAll") { t ->
      t.group = VERIFICATION_GROUP
      t.dependsOn(testTasks)
    }

    testTasks.configureEach { t ->
      t.systemProperty("kotlinx.coroutines.test.default_timeout", "20s")

      t.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 4).coerceIn(1, 4)

      // Suppresses mockk warning - see https://github.com/mockk/mockk/issues/1171
      t.jvmArgs("-XX:+EnableDynamicAgentLoading")

      // Robolectric 4.17 reflects into jdk.internal.access.SharedSecrets for FileDescriptor
      // interception
      t.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")

      // Robolectric downloads android-all jars at runtime, outside the Gradle cache. Maven Central
      // rate-limits CI with 429s, so use Google's mirror of it (#2247)
      t.systemProperty(
        "robolectric.dependency.repo.url",
        "https://maven-central.storage-download.googleapis.com/maven2",
      )

      // To work around https://github.com/gradle/gradle/issues/33619
      t.failOnNoDiscoveredTests.set(false)

      // Retry flaky failures on CI, but not when many tests fail at once
      t.extensions.configure(TestRetryTaskExtension::class) {
        maxRetries.set(if (isCi) 2 else 0)
        maxFailures.set(10)
        failOnPassedAfterRetry.set(false)
      }

      if (t.name.contains("release", ignoreCase = true)) {
        t.enabled = false
      }

      t.testLogging { tl ->
        tl.events = setOf(SKIPPED, FAILED)
        tl.exceptionFormat = FULL
        tl.showCauses = true
        tl.showExceptions = true
        tl.showStackTraces = true
        tl.showStandardStreams = false
        tl.displayGranularity = 2
      }
    }
  }
}
