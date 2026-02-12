package aktual.gradle

import blueprint.core.intProperty
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
  override fun apply(target: Project): Unit =
    with(target) {
      pluginManager.apply(KoverGradlePlugin::class)

      extensions.configure<KoverProjectExtension> {
        useJacoco.set(false)

        reports {
          total {
            filters.excludes {
              androidGeneratedClasses()

              classes("*Application*", "*Preview*Kt*")

              packages("*.di.*", "*.generated.*")

              annotatedBy(
                "aktual.core.ui.DesktopPreview",
                "aktual.core.ui.LandscapePreview",
                "aktual.core.ui.PortraitPreview",
                "aktual.core.ui.TabletPreview",
                "androidx.compose.runtime.Composable",
                "androidx.compose.ui.tooling.preview.Preview",
                "dev.zacsweers.metro.BindingContainer",
                "javax.annotation.processing.Generated",
              )

              projects.addAll(":aktual-codegen:ksp", ":aktual-core:l10n", ":aktual-core:ui")
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
              minValue = providers.intProperty("aktual.kover.minCoverage").get(),
              coverageUnits = INSTRUCTION,
              aggregationForGroup = COVERED_PERCENTAGE,
            )
          }
        }
      }

      // Include this module in test coverage
      // TODO: use isolated projects
      rootProject.dependencies { "kover"(project) }
    }
}
