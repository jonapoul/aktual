import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:vm"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-account:domain"))
    implementation(project(":aktual-l10n"))
    implementation(project(":aktual-logging"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines.core)
  }
}
