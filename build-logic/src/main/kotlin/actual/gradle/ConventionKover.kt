package actual.gradle

import blueprint.core.intProperty
import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import kotlinx.kover.gradle.plugin.dsl.KoverReportsConfig
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ConventionKover : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(KoverGradlePlugin::class.java)
    }

    extensions.configure<KoverProjectExtension> {
      useJacoco = false
      reports { configureKoverReports(project) }
    }

    rootProject.dependencies {
      // Include this module in test coverage
      val kover = rootProject.configurations.getByName("kover")
      kover(project)
    }
  }

  private fun KoverReportsConfig.configureKoverReports(project: Project) {
    total {
      filters {
        excludes {
          classes(
            "*Activity*",
            "*Application*",
            "*BuildConfig*",
            "*_Factory*",
            "*_HiltModules*",
            "*_Impl*",
            "*Module_*",
            "hilt_aggregated_deps*",
            "*ComposableSingletons*",
            "*Preview*Kt*",
          )

          annotatedBy(
            "androidx.compose.runtime.Composable",
            "dagger.Generated",
            "dagger.Module",
            "dagger.Provides",
            "javax.annotation.processing.Generated",
          )
        }
      }

      log {
        onCheck = true
        coverageUnits = CoverageUnit.INSTRUCTION
        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
      }
    }

    verify {
      rule {
        disabled = project != project.rootProject
        bound {
          minValue = project.intProperty(key = "blueprint.kover.minCoverage")
          coverageUnits = CoverageUnit.INSTRUCTION
          aggregationForGroup = AggregationType.COVERED_PERCENTAGE
        }
      }
    }
  }
}

fun Project.koverExcludes(config: Action<KoverReportFilter>) {
  extensions.configure<KoverProjectExtension> {
    reports {
      total {
        filters {
          excludes(config)
        }
      }
    }
  }
}
