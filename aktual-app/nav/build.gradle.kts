import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    // Don't change these UI modules away from API - they're needed for DI graph resolution from
    // app
    // layer
    api(project(":aktual-about:ui"))
    api(project(":aktual-account:ui"))
    api(project(":aktual-budget:list:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:sync:ui"))
    api(project(":aktual-budget:transactions:ui"))
    api(project(":aktual-core:api"))
    api(project(":aktual-core:l10n"))
    api(project(":aktual-core:logging"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-metrics:ui"))
    api(project(":aktual-settings:ui"))
    api(libs.jetbrains.navigation)
    api(libs.jetbrains.resources)
    api(libs.jetbrains.runtime)
    api(libs.molecule)
    implementation(project(":aktual-core:ui"))
    implementation(libs.jetbrains.material3)
    implementation(libs.jetbrains.savedstate)
    implementation(libs.logcat)
  }
}
