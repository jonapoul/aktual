package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.getVersion
import blueprint.core.libs
import blueprint.recipes.composeBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinJvm::class.java)
      apply(ConventionAndroidBase::class.java)
    }

    composeBlueprint(
      composeBomVersion = libs.getVersion("androidx.compose.bom"),
      composeLintVersion = libs.getVersion("androidx.compose.lint"),
      writeMetrics = true,
      experimentalApis = emptyList(),
    )

    val androidTestImplementation by configurations
    val debugImplementation by configurations
    val implementation by configurations
    val lintChecks by configurations

    dependencies {
      implementation(platform(libs.getLibrary("androidx.compose.bom")))
      lintChecks(libs.getLibrary("androidx.compose.lint"))

      // Testing
      debugImplementation(libs.getLibrary("test.androidx.compose.ui.manifest"))

      // UI testing
      androidTestImplementation(platform(libs.getLibrary("androidx.compose.bom")))
      androidTestImplementation(libs.getLibrary("test.androidx.compose.ui.junit4"))
    }
  }
}
