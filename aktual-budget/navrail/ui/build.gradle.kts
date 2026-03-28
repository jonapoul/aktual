import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:navrail:vm"))
    implementation(project(":aktual-app:nav"))
    implementation(project(":aktual-budget:model"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-core:ui"))
    implementation(libs.compose.navigation3)
    implementation(libs.compose.savedstate)
    implementation(libs.compose.viewmodelNavigation3)
  }
}
