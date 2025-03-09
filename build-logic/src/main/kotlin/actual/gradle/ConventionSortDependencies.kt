package actual.gradle

import blueprint.recipes.sortDependenciesBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionSortDependencies : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    sortDependenciesBlueprint()
  }
}
