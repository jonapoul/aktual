import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:account:vm"))
    api(project(":modules:core:model"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:account:domain"))
    implementation(project(":modules:l10n"))
    implementation(project(":modules:logging"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines)
  }
}
