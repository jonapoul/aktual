package aktual.gradle

import blueprint.core.get
import blueprint.core.libs
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LintPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
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

    extensions.findByType(CommonExtension::class)?.apply {
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

      targetKotlinPlatforms.addAll(
        KotlinPlatformType.common,
        KotlinPlatformType.jvm,
        KotlinPlatformType.androidJvm,
      )
    }

    val lintChecks = configurations.findByName("lintChecks") ?: run {
      pluginManager.apply(LintPlugin::class)
      configurations.getByName("lintChecks")
    }

    dependencies {
      lintChecks(libs["androidx.compose.lint"])
    }
  }
}
