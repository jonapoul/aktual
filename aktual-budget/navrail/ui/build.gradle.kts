plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.compose.animation)
    api(libs.compose.foundation)
    api(project(":aktual-budget:demo"))
    api(project(":aktual-budget:navrail:vm"))
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.compose.navigation3.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.viewmodelNavigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
