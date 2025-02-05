package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradleExtension
import org.jetbrains.kotlin.powerassert.gradle.PowerAssertGradlePlugin

@OptIn(ExperimentalKotlinGradlePluginApi::class)
class ConventionTestPowerAssert : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(PowerAssertGradlePlugin::class.java)
    }

    extensions.configure<PowerAssertGradleExtension> {
      functions = setOf(
        "kotlin.assert",
        "kotlin.test.assertEquals",
        "kotlin.test.assertFalse",
        "kotlin.test.assertIs",
        "kotlin.test.assertNotNull",
        "kotlin.test.assertNull",
        "kotlin.test.assertTrue",
      )
    }
  }
}
