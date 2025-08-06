package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import commonMainDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ModuleDi : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply(ConventionDi::class.java)
    }

    extensions.configure<KotlinMultiplatformExtension> {
      commonMainDependencies {
        implementation(libs.getLibrary("metro.runtime"))
      }
    }
  }
}
