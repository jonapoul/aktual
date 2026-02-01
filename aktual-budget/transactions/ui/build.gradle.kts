import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:transactions:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-l10n"))
    implementation(libs.androidx.paging.compose)
    implementation(libs.kotlinx.datetime)
    implementation(libs.shimmer)
  }
}
