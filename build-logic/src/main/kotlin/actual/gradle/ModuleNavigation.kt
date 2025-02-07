package actual.gradle

import blueprint.core.getLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleNavigation : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply("org.jetbrains.kotlin.plugin.serialization")
    }

    commonMainDependencies {
      implementation(libs.getLibrary("kotlinx.serialization.core"))
      compileOnly(libs.getLibrary("alakazam.kotlin.compose.annotations"))
    }
  }
}
