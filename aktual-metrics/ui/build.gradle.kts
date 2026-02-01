import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-metrics:vm"))
    implementation(project(":aktual-l10n"))
    implementation(libs.haze.core)
  }
}
