plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(project(":aktual-budget:reports:vm"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.vico)
    implementation(project(":aktual-budget:reports:components"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
  }
}
