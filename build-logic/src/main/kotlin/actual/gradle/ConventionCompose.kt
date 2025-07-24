package actual.gradle

import blueprint.core.getLibrary
import blueprint.core.libs
import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.androidJvm

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinBase::class.java)
      apply(ConventionAndroidBase::class.java)
      apply(ComposeCompilerGradleSubplugin::class)
    }

    extensions.getByType(CommonExtension::class).apply {
      buildFeatures {
        compose = true
      }
    }

    val metricReportDir = project.layout.buildDirectory.dir("compose_metrics")
    val stabilityFile = rootProject.layout.projectDirectory.file("config/compose-stability.conf")

    extensions.configure<ComposeCompilerGradlePluginExtension> {
      metricsDestination.set(metricReportDir)
      reportsDestination.set(metricReportDir)

      stabilityConfigurationFiles.add(stabilityFile)

      targetKotlinPlatforms.addAll(androidJvm)
    }

    dependencies {
      "implementation"(platform(libs.getLibrary("androidx.compose.bom")))
      "lintChecks"(libs.getLibrary("androidx.compose.lint"))

      // Testing
      "testImplementation"(libs.getLibrary("test.androidx.compose.ui.junit4"))
      "testImplementation"(libs.getLibrary("test.robolectric"))
      "debugImplementation"(libs.getLibrary("test.androidx.compose.ui.manifest"))
    }
  }
}
