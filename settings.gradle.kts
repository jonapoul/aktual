import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType

rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("build-logic")

  // Repositories for resolving the plugins {} block below must be declared in a literal
  // pluginManagement block. Gradle evaluates that block before it runs apply(from = ...), so the
  // repos in gradle/repositories.gradle.kts aren't visible here. Only the repos those plugins need:
  // google() for AGP, the portal for everything else.
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    gradlePluginPortal()
  }
}

// https://github.com/autonomousapps/dependency-analysis-gradle-plugin/issues/1661#issue-4167340036
buildscript {
  dependencies {
    classpath("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.10")
  }
}

plugins {
  id("com.android.application") version "9.3.1" apply false
  id("com.android.kotlin.multiplatform.library") version "9.3.1" apply false
  id("com.autonomousapps.build-health") version "3.17.0"
  id("com.gradle.develocity") version "4.5.0"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("org.jetbrains.kotlin.jvm") version "2.4.10" apply false
  id("org.jetbrains.kotlin.multiplatform") version "2.4.10" apply false
  id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.9"
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
    excludedProjects.addAll(":aktual-core:l10n", ":aktual-core:ui")
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
  ":aktual-about:ui",
  ":aktual-about:vm",
  ":aktual-account:domain",
  ":aktual-account:ui",
  ":aktual-account:vm",
  ":aktual-api",
  ":aktual-api:di",
  ":aktual-api:impl",
  ":aktual-api:model",
  ":aktual-app:android",
  ":aktual-app:desktop",
  ":aktual-app:di",
  ":aktual-app:nav",
  ":aktual-app:ui-app",
  ":aktual-app:ui-budget",
  ":aktual-budget",
  ":aktual-budget:data:db",
  ":aktual-budget:data:encryption",
  ":aktual-budget:data:impl",
  ":aktual-budget:data:proto",
  ":aktual-budget:list:ui",
  ":aktual-budget:list:vm",
  ":aktual-budget:model",
  ":aktual-budget:navrail:ui",
  ":aktual-budget:navrail:vm",
  ":aktual-budget:reports:ui",
  ":aktual-budget:reports:vm",
  ":aktual-budget:rules:ui",
  ":aktual-budget:rules:vm",
  ":aktual-budget:schedules:ui",
  ":aktual-budget:schedules:vm",
  ":aktual-budget:sync:domain",
  ":aktual-budget:sync:ui",
  ":aktual-budget:sync:vm",
  ":aktual-budget:tags:ui",
  ":aktual-budget:tags:vm",
  ":aktual-budget:transactions:ui",
  ":aktual-budget:transactions:vm",
  ":aktual-core",
  ":aktual-core:icons",
  ":aktual-core:l10n",
  ":aktual-core:logging",
  ":aktual-core:logging:impl",
  ":aktual-core:model",
  ":aktual-core:nav",
  ":aktual-core:theme",
  ":aktual-core:theme:impl",
  ":aktual-core:theme:model",
  ":aktual-core:ui",
  ":aktual-di:bindings",
  ":aktual-di:core",
  ":aktual-di:graphs",
  ":aktual-di:runlevel",
  ":aktual-di:runlevel:impl",
  ":aktual-metrics:ui",
  ":aktual-metrics:vm",
  ":aktual-prefs",
  ":aktual-prefs:impl",
  ":aktual-prefs:ui",
  ":aktual-prefs:vm",
  ":aktual-test",
  ":aktual-test:api",
  ":aktual-test:compose",
  ":aktual-test:smoke",
  ":detekt-rules",
)
