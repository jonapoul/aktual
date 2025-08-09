package actual.gradle

import blueprint.core.boolPropertyOrElse
import blueprint.recipes.koverBlueprint
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import kotlinx.kover.gradle.plugin.dsl.KoverReportFilter
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ConventionKover : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    if (!boolPropertyOrElse(key = "actual.includeInKover", default = true)) {
      return@with
    }

    val excludedAnnotations = listOf(
      "actual.core.ui.SingleScreenPreview",
      "actual.core.ui.TripleScreenPreview",
      "androidx.compose.ui.tooling.preview.Preview",
      "javax.annotation.processing.Generated",
    )
    val excludedClasses = listOf(
      "*Activity*",
      "*Application*",
      "*BuildConfig*",
      "*Preview*Kt*",
    )
    val excludedPackages = listOf(
      "*.di.*",
    )

    koverBlueprint(
      useJacoco = false,
      excludedAnnotations = excludedAnnotations,
      excludedClasses = excludedClasses,
      excludedPackages = excludedPackages,
    )
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
