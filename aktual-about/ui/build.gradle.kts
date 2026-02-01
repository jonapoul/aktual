import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-about:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-l10n"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.haze.core)
  }
}
