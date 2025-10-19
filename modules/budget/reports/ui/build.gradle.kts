import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:reports:vm"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:core:model"))
    implementation(project(":modules:l10n"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.markdown.core)
    implementation(libs.markdown.m3)
    implementation(libs.vico.multiplatform)
  }
}
