package aktual.gradle

import app.cash.burst.gradle.BurstPlugin
import blueprint.recipes.DEFAULT_POWER_ASSERT_FUNCTIONS
import blueprint.recipes.testBaseBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradlePlugin

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class ConventionTest : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionTestDependencies::class)
      apply(PowerAssertGradlePlugin::class.java)
    }

    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
      pluginManager.apply(BurstPlugin::class)
    }

    testBaseBlueprint()

    extensions.configure<PowerAssertGradleExtension> {
      functions.addAll(DEFAULT_POWER_ASSERT_FUNCTIONS)
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
        showStandardStreams = true
        displayGranularity = 2
      }
    }
  }
}
