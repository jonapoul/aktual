package aktual.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinBase::class)
      apply(ComposeCompilerGradleSubplugin::class)
      apply(ComposePlugin::class)
    }

    pluginManager.withPlugin("com.android.base") {
      extensions.configure(CommonExtension::class) {
        buildFeatures.compose = true
      }
    }

    val metricReportDir = project.layout.buildDirectory.dir("compose_metrics")
    val stabilityFile = rootProject.layout.projectDirectory.file("config/compose-stability.conf")

    extensions.configure<ComposeCompilerGradlePluginExtension> {
      metricsDestination.set(metricReportDir)
      reportsDestination.set(metricReportDir)
      stabilityConfigurationFiles.add(stabilityFile)

      targetKotlinPlatforms.addAll(
        KotlinPlatformType.common,
        KotlinPlatformType.jvm,
        KotlinPlatformType.androidJvm,
      )
    }

    pluginManager.withPlugin("com.android.lint") {
      dependencies {
        "lintChecks"(libs["androidx.compose.lint"])
      }
    }
  }
}
