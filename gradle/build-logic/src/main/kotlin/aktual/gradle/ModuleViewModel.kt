package aktual.gradle

import blueprint.core.get
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
        api(project(":aktual-core:di"))
        api(libs["androidx.lifecycle.viewmodel.core"])
        api(libs["kotlinx.coroutines.core"])
        api(libs["kotlinx.immutable"])
        implementation(project(":aktual-logging"))
        implementation(libs["jetbrains.runtime"])
        implementation(libs["molecule"])
      }
    }
  }
}
