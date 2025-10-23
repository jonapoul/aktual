plugins {
  alias(libs.plugins.module.compose)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    // Don't change these UI modules away from API - they're needed for DI graph resolution from app layer
    api(project(":modules:about:ui"))
    api(project(":modules:account:ui"))
    api(project(":modules:budget:list:ui"))
    api(project(":modules:budget:reports:ui"))
    api(project(":modules:budget:sync:ui"))
    api(project(":modules:budget:transactions:ui"))
    api(project(":modules:core:connection"))
    api(project(":modules:core:ui"))
    api(project(":modules:l10n"))
    api(project(":modules:logging"))
    api(project(":modules:settings:ui"))
    api(compose.components.resources)
    api(compose.runtime)
    api(libs.jetbrains.navigation)
    api(libs.molecule)
    implementation(project(":modules:core:ui"))
    implementation(compose.material3)
    implementation(libs.jetbrains.savedstate)
    implementation(libs.logcat)
  }
}
