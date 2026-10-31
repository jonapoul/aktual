plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(project(":aktual-budget:accounts:vm"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
  }
}
