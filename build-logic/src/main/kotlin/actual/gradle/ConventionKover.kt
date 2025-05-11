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

    koverBlueprint(
      excludedClasses = listOf(
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
      ),
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
