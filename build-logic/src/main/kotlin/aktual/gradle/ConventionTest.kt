package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import aktual.gradle.dsl.withType
import app.cash.burst.gradle.BurstPlugin
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigPlugin
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class ConventionTest : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      with(pluginManager) {
        apply(ConventionTestDependencies::class)
        apply(BuildConfigPlugin::class)
      }

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

        // Suppresses mockk warning - see https://github.com/mockk/mockk/issues/1171
        t.jvmArgs("-XX:+EnableDynamicAgentLoading")

        // To work around https://github.com/gradle/gradle/issues/33619
        t.failOnNoDiscoveredTests.set(false)

        if (t.name.contains("release", ignoreCase = true)) {
          t.enabled = false
        }

        t.testLogging { tl ->
          tl.events = setOf(PASSED, SKIPPED, FAILED)
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
