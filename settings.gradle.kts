rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("gradle/build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.3.1"
}

develocity.buildScan {
  if (!gradle.startParameter.isBuildScan) {
    publishing.onlyIf { it.isAuthenticated }
  }

  uploadInBackground = false
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
  ":aktual-budget:di",
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
  ":aktual-core:l10n",
  ":aktual-core:logging",
  ":aktual-core:model",
  ":aktual-core:prefs",
  ":aktual-core:ui",
  ":aktual-metrics:ui",
  ":aktual-metrics:vm",
  ":aktual-settings:ui",
  ":aktual-settings:vm",
  ":aktual-test:android",
  ":aktual-test:compose",
  ":aktual-test:kotlin",
)
