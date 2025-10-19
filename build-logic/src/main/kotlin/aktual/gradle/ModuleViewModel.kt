package aktual.gradle

import blueprint.core.invoke
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
        api(libs("androidx.lifecycle.viewmodel.core"))
        api(libs("kotlinx.coroutines.core"))
        api(libs("kotlinx.immutable"))
        implementation(project(":modules:logging"))
        implementation(libs("molecule"))
        implementation(compose.runtime)
      }
    }
  }
}
