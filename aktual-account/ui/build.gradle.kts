import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:vm"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-account:domain"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:logging"))
    implementation(libs.haze.core)
  }
}
