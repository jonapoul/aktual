import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement { includeBuild("gradle/build-logic") }

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.3.2"
  id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.7"
}

develocity.buildScan {
  if (!gradle.startParameter.isBuildScan) {
    publishing.onlyIf { it.isAuthenticated }
  }

  uploadInBackground = false
}

kover {
  enableCoverage()

  reports {
    excludedClasses.addAll("*Application*", "*Preview*Kt*")
    excludedProjects.addAll(":aktual-codegen:ksp", ":aktual-core:l10n", ":aktual-core:ui")
    excludesAnnotatedBy.addAll(
      "aktual.core.ui.DesktopPreview",
      "aktual.core.ui.LandscapePreview",
      "aktual.core.ui.PortraitPreview",
      "aktual.core.ui.TabletPreview",
      "androidx.compose.runtime.Composable",
      "androidx.compose.ui.tooling.preview.Preview",
      "dev.zacsweers.metro.BindingContainer",
      "javax.annotation.processing.Generated",
    )

    verify.rule {
      groupBy = GroupingEntityType.APPLICATION

      bound {
        minValue = 35
        maxValue = 100
        coverageUnits = CoverageUnit.INSTRUCTION
        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
      }
    }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(
  ":aktual-about:data",
  ":aktual-about:di",
  ":aktual-about:ui",
  ":aktual-about:vm",
  ":aktual-account:domain",
  ":aktual-account:ui",
  ":aktual-account:vm",
  ":aktual-app:android",
  ":aktual-app:desktop",
  ":aktual-app:di",
  ":aktual-app:nav",
  ":aktual-budget:data",
  ":aktual-budget:data-di",
  ":aktual-budget:data-impl",
  ":aktual-budget:list:ui",
  ":aktual-budget:list:vm",
  ":aktual-budget:model",
  ":aktual-budget:reports:ui",
  ":aktual-budget:reports:vm",
  ":aktual-budget:sync:ui",
  ":aktual-budget:sync:vm",
  ":aktual-budget:transactions:ui",
  ":aktual-budget:transactions:vm",
  ":aktual-codegen:annotation",
  ":aktual-codegen:ksp",
  ":aktual-core:api",
  ":aktual-core:di",
  ":aktual-core:icons",
  ":aktual-core:l10n",
  ":aktual-core:logging",
  ":aktual-core:model",
  ":aktual-core:prefs",
  ":aktual-core:prefs-di",
  ":aktual-core:prefs-impl",
  ":aktual-core:theme",
  ":aktual-core:theme:di",
  ":aktual-core:theme:impl",
  ":aktual-core:ui",
  ":aktual-metrics:ui",
  ":aktual-metrics:vm",
  ":aktual-settings:ui",
  ":aktual-settings:vm",
  ":aktual-test:android",
  ":aktual-test:compose",
  ":aktual-test:kotlin",
)
