package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import commonMainDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ModuleDi : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class)
      apply(ConventionDi::class)
    }

    kotlin {
      commonMainDependencies {
        implementation(libs.getLibrary("metro.runtime"))
      }
    }
  }
}
