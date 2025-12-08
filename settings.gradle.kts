rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("gradle/build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("com.gradle.develocity") version "4.2.2"
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
  ":aktual-about:ui",
  ":aktual-about:vm",
  ":aktual-account:domain",
  ":aktual-account:ui",
  ":aktual-account:vm",
  ":aktual-api:actual",
  ":aktual-api:builder",
  ":aktual-api:github",
  ":aktual-app:android",
  ":aktual-app:desktop",
  ":aktual-app:di",
  ":aktual-app:nav",
  ":aktual-budget:data",
  ":aktual-budget:encryption",
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
  ":aktual-core:connection",
  ":aktual-core:di",
  ":aktual-core:model",
  ":aktual-core:ui",
  ":aktual-l10n",
  ":aktual-logging",
  ":aktual-metrics:ui",
  ":aktual-metrics:vm",
  ":aktual-prefs",
  ":aktual-settings:ui",
  ":aktual-settings:vm",
  ":aktual-test:android",
  ":aktual-test:compose",
  ":aktual-test:kotlin",
)
