package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.kotlin.dsl.withType

class ConventionTest : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionTestPowerAssert::class.java)
      apply(ConventionTestDependencies::class.java)
    }

    tasks.withType<Test> {
      if (name.contains("release", ignoreCase = true)) {
        enabled = false
      }

      // Suppresses mockk warning - see https://github.com/mockk/mockk/issues/1171
      jvmArgs("-XX:+EnableDynamicAgentLoading")

      testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
      }
    }
  }
}
