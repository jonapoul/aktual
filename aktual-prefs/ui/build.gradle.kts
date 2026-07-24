import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.foundation)
    api(project(":aktual-core:model"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-prefs:vm"))
    implementation(libs.compose.resources)
    implementation(libs.metrox.viewmodel)
    implementation(libs.shimmer)
    implementation(project(":aktual-core:l10n"))
  }
}
