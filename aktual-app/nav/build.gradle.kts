plugins {
  alias(libs.plugins.module.compose)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    // Don't change these UI modules away from API - they're needed for DI graph resolution from app layer
    api(project(":aktual-about:ui"))
    api(project(":aktual-account:ui"))
    api(project(":aktual-budget:list:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:sync:ui"))
    api(project(":aktual-budget:transactions:ui"))
    api(project(":aktual-core:connection"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-l10n"))
    api(project(":aktual-logging"))
    api(project(":aktual-settings:ui"))
    api(compose.components.resources)
    api(compose.runtime)
    api(libs.jetbrains.navigation)
    api(libs.molecule)
    implementation(project(":aktual-core:ui"))
    implementation(compose.material3)
    implementation(libs.jetbrains.savedstate)
    implementation(libs.logcat)
  }
}
