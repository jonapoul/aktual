package actual.gradle

import blueprint.core.getLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

class ModuleNavigation : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply("org.jetbrains.kotlin.plugin.serialization")
    }

    val implementation by configurations
    commonMainDependencies {
      implementation(libs.getLibrary("kotlinx.serialization.core"))
    }
  }
}
