import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:about:vm"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:l10n"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines)
  }
}
