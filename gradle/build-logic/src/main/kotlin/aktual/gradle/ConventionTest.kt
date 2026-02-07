package aktual.gradle

import app.cash.burst.gradle.BurstPlugin
import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.buildConfigField
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.io.File

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class ConventionTest : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionTestDependencies::class)
      apply(BuildConfigPlugin::class)
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
      pluginManager.apply(BurstPlugin::class)
    }

    extensions.configure(BuildConfigExtension::class) {
      sourceSets.named("test") {
        forClass("Resources") {
          packageName("aktual.test")
          useKotlinOutput { topLevelConstants = true }
          buildConfigField<File>("RESOURCES_DIR", layout.projectDirectory.dir("src/commonTest/resources").asFile)
        }
      }
    }

    tasks.withType<Test>().configureEach {
      // Suppresses mockk warning - see https://github.com/mockk/mockk/issues/1171
      jvmArgs("-XX:+EnableDynamicAgentLoading")

      // To work around https://github.com/gradle/gradle/issues/33619
      failOnNoDiscoveredTests.set(false)

      if (name.contains("release", ignoreCase = true)) {
        enabled = false
      }

      testLogging {
        events = setOf(PASSED, SKIPPED, FAILED)
        exceptionFormat = FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = false
        displayGranularity = 2
      }
    }
  }
}
