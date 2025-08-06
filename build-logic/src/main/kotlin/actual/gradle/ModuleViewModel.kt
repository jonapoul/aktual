package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import commonMainDependencies
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ModuleViewModel : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class)
      apply(ConventionCompose::class)
    }

    extensions.configure<KotlinMultiplatformExtension> {
      commonMainDependencies {
        api(project(":modules:di:core"))
        api(libs.getLibrary("androidx.lifecycle.viewmodel.core"))
        implementation(project(":modules:logging"))
        implementation(libs.getLibrary("kotlinx.coroutines"))
        implementation(libs.getLibrary("molecule"))
      }
    }
  }
}
