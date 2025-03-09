package actual.gradle

import blueprint.recipes.licenseeBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionLicensee : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    licenseeBlueprint()
  }
}
