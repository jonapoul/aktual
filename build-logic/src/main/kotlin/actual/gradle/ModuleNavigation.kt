package actual.gradle

import blueprint.core.commonMainDependencies
import blueprint.core.getLibrary
import blueprint.core.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ModuleNavigation : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply("org.jetbrains.kotlin.plugin.serialization")
    }

    extensions.configure<KotlinMultiplatformExtension> {
      commonMainDependencies {
        implementation(libs.getLibrary("kotlinx.serialization.core"))
        compileOnly(libs.getLibrary("alakazam.kotlin.compose.annotations"))
      }
    }
  }
}
