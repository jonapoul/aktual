import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:reports:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-l10n"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.markdown.core)
    implementation(libs.markdown.m3)
    implementation(libs.vico.multiplatform)
  }
}
