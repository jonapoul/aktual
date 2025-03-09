package actual.gradle

import blueprint.recipes.ideaBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionIdea : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    ideaBlueprint()
  }
}
