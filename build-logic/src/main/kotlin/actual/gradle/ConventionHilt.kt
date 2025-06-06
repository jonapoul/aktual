package actual.gradle

import blueprint.core.boolPropertyOrElse
import blueprint.core.getLibrary
import blueprint.core.libs
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate

class ConventionHilt : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(KspGradleSubplugin::class.java)
    }

    val ksp by configurations
    val kspTest by configurations
    val testImplementation by configurations

    val applyHiltCompiler = boolPropertyOrElse("actual.hilt.applyCompiler", default = true)

    dependencies {
      if (applyHiltCompiler) {
        ksp(libs.getLibrary("hilt.compiler"))
        kspTest(libs.getLibrary("hilt.compiler"))
      }
      testImplementation(libs.getLibrary("test.hilt"))
    }
  }
}
