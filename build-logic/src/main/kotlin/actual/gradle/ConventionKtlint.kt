package actual.gradle

import blueprint.core.getVersion
import blueprint.recipes.ktlintBlueprint
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionKtlint : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    ktlintBlueprint(libs.getVersion("ktlint.cli"))
  }
}
