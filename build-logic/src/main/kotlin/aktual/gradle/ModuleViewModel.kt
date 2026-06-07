package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.kotlin
import blueprint.core.commonMainDependencies
import blueprint.core.get
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleViewModel : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      with(pluginManager) {
        apply(ModuleKotlin::class)
        apply(ConventionCompose::class)
      }

      kotlin {
        commonMainDependencies {
          api(libs["androidx.lifecycle.viewmodel"])
          api(libs["androidx.lifecycle.viewmodel.savedstate"])
          api(libs["kotlinx.coroutines.core"])
          api(libs["kotlinx.immutable"])
          api(libs["metrox.viewmodel"])
          implementation(project(":aktual-core:logging"))
          implementation(libs["compose.runtime"])
          implementation(libs["molecule"])
        }
      }
    }
}
