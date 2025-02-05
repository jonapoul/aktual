package actual.gradle

import blueprint.core.getLibrary
import blueprint.recipes.DetektAll
import blueprint.recipes.detektBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    detektBlueprint(
      detektAllConfig = DetektAll.Apply(ignoreRelease = true),
    )

    val detektPlugins by configurations
    dependencies {
      detektPlugins(libs.getLibrary("plugin.detektCompose"))
    }
  }
}
