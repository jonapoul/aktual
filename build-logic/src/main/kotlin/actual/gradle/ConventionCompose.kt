package actual.gradle

import blueprint.core.getLibrary
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinJvm::class.java)
      apply(ConventionAndroidBase::class.java)
      apply(ComposeCompilerGradleSubplugin::class.java)
    }

    extensions.findByType(CommonExtension::class)?.apply {
      buildFeatures {
        compose = true
      }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {
      val metricReportDir = project.layout.buildDirectory.dir("compose_metrics").get().asFile
      metricsDestination = metricReportDir
      reportsDestination = metricReportDir
      stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.conf"))
      targetKotlinPlatforms = setOf(KotlinPlatformType.androidJvm)
    }

    val androidTestImplementation by configurations
    val debugImplementation by configurations
    val implementation by configurations
    val lintChecks by configurations

    dependencies {
      implementation(platform(libs.getLibrary("androidx.compose.bom")))
      lintChecks(libs.getLibrary("androidx.compose.lint"))

      // Testing
      debugImplementation(libs.getLibrary("test.androidx.compose.ui.manifest"))

      // UI testing
      androidTestImplementation(platform(libs.getLibrary("androidx.compose.bom")))
      androidTestImplementation(libs.getLibrary("test.androidx.compose.ui.junit4"))
    }
  }
}
