package actual.gradle

import blueprint.core.getLibrary
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

class ModuleNavigation : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleKotlin::class.java)
      apply("org.jetbrains.kotlin.plugin.serialization")
    }

    val implementation by configurations
    dependencies {
      implementation(libs.getLibrary("kotlinx.serialization.core"))
    }
  }
}
