package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import blueprint.recipes.DetektAll
import blueprint.recipes.detektBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ConventionDetekt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    detektBlueprint(
      detektAllConfig = DetektAll.Apply(ignoreRelease = true),
    )

    dependencies {
      "detektPlugins"(libs.getLibrary("plugin.detektCompose"))
    }
  }
}
