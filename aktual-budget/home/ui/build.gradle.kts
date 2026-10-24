plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(project(":aktual-budget:home:vm"))
    api(project(":aktual-core:nav"))
    implementation(libs.metrox.viewmodel)
    implementation(libs.shimmer)
    implementation(project(":aktual-budget:accounts:ui"))
    implementation(project(":aktual-budget:reports:ui"))
    implementation(project(":aktual-core:l10n"))
  }
}
