package actual.gradle

import blueprint.recipes.powerAssertBlueprint
import blueprint.recipes.testBaseBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class ConventionTest : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionTestDependencies::class.java)
    }

    testBaseBlueprint()
    powerAssertBlueprint()

    tasks.withType<Test> {
      // Suppresses mockk warning - see https://github.com/mockk/mockk/issues/1171
      jvmArgs("-XX:+EnableDynamicAgentLoading")
    }
  }
}
