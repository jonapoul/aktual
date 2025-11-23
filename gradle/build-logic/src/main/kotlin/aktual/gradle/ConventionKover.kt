package aktual.gradle

import kotlinx.kover.gradle.plugin.KoverGradlePlugin
import kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class ConventionKover : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    val includeInKover = boolProperty("aktual.includeInKover").getOrElse(true)
    if (!includeInKover) return

    pluginManager.apply(KoverGradlePlugin::class)

    extensions.configure<KoverProjectExtension> {
      useJacoco.set(false)

      reports {
        total {
          filters.excludes {
            androidGeneratedClasses()

            classes(
              "*Activity*",
              "*Application*",
              "*BuildConfig*",
              "*Preview*Kt*",
            )

            packages(
              "*.di.*",
              "*.generated.*",
            )

            annotatedBy(
              "aktual.core.ui.SingleScreenPreview",
              "aktual.core.ui.TripleScreenPreview",
              "androidx.compose.runtime.Composable",
              "androidx.compose.ui.tooling.preview.Preview",
              "javax.annotation.processing.Generated",
            )
          }

          html.onCheck.set(true)

          log {
            onCheck.set(true)
            format.set("<value>")
            coverageUnits.set(INSTRUCTION)
            aggregationForGroup.set(COVERED_PERCENTAGE)
          }
        }

        verify.rule {
          disabled.set(project != project.rootProject)
          minBound(
            minValue = intProperty("aktual.kover.minCoverage").get(),
            coverageUnits = INSTRUCTION,
            aggregationForGroup = COVERED_PERCENTAGE,
          )
        }
      }
    }

    // Include this module in test coverage
    rootProject.dependencies { "kover"(project) }
  }
}
