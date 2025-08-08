import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:reports:vm"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:account:model"))
    implementation(project(":modules:l10n"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.markdown.core)
    implementation(libs.markdown.m3)
    implementation(libs.vico.multiplatform)
  }
}
