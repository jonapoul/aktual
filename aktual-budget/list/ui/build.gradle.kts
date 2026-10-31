plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(project(":aktual-budget:list:vm"))
    api(project(":aktual-core:nav"))
    implementation(libs.shimmer)
    implementation(project(":aktual-budget:sync:ui"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
