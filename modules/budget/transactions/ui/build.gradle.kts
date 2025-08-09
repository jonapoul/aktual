import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(project(":modules:budget:transactions:vm"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:account:model"))
    implementation(project(":modules:l10n"))
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
  }
}
