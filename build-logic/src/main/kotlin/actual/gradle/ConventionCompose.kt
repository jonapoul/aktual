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
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinJvm::class.java)
      apply(ConventionAndroidBase::class.java)
    }

    tasks.withType<KotlinCompile>().configureEach {
      compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.ui.test.ExperimentalTestApi")
      }
    }

    composeBlueprint(
      composeBomVersion = libs.getVersion("androidx.compose.bom"),
      composeLintVersion = libs.getVersion("androidx.compose.lint"),
      writeMetrics = true,
      experimentalApis = emptyList(),
    )

    val debugImplementation by configurations
    val testImplementation by configurations
    val implementation by configurations
    val lintChecks by configurations

    dependencies {
      implementation(platform(libs.getLibrary("androidx.compose.bom")))
      lintChecks(libs.getLibrary("androidx.compose.lint"))

      // Testing
      testImplementation(libs.getLibrary("test.androidx.compose.ui.junit4"))
      testImplementation(libs.getLibrary("test.robolectric"))
      debugImplementation(libs.getLibrary("test.androidx.compose.ui.manifest"))
    }
  }
}
