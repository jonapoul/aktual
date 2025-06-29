package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.getVersion
import blueprint.core.libs
import blueprint.recipes.composeBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

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
      stabilityFile = rootProject.layout.projectDirectory.file("config/compose-stability.conf"),
    )

    dependencies {
      "implementation"(platform(libs.getLibrary("androidx.compose.bom")))
      "lintChecks"(libs.getLibrary("androidx.compose.lint"))

      // Testing
      "testImplementation"(libs.getLibrary("test.androidx.compose.ui.junit4"))
      "testImplementation"(libs.getLibrary("test.robolectric"))
      "debugImplementation"(libs.getLibrary("test.androidx.compose.ui.manifest"))
    }
  }
}
