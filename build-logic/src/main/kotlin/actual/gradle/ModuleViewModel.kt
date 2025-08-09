package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import commonMainDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ModuleViewModel : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class)
      apply(ConventionCompose::class)
    }

    kotlin {
      commonMainDependencies {
        api(project(":modules:core:di"))
        api(libs.getLibrary("androidx.lifecycle.viewmodel.core"))
        api(libs.getLibrary("kotlinx.coroutines"))
        api(libs.getLibrary("kotlinx.immutable"))
        implementation(project(":modules:logging"))
        implementation(libs.getLibrary("molecule"))
        implementation(compose.runtime)
      }
    }
  }
}
