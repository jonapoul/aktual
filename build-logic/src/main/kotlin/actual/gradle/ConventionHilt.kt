package actual.gradle

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
    val androidTestImplementation by configurations

    dependencies {
      ksp(libs.getLibrary("hilt.compiler"))
      kspTest(libs.getLibrary("hilt.compiler"))

      testImplementation(libs.getLibrary("test.hilt"))
      androidTestImplementation(libs.getLibrary("test.hilt"))
    }
  }
}
