import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:core:model"))
    api(project(":modules:core:ui"))
    api(project(":modules:settings:vm"))
    implementation(project(":modules:l10n"))
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines.core)
  }
}
