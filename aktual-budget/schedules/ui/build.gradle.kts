plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.foundation)
    api(project(":aktual-budget:schedules:vm"))
    api(project(":aktual-core:nav"))
    implementation(libs.compose.resources)
    implementation(libs.kotlinx.datetime)
    implementation(libs.shimmer)
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
    implementation(project(":aktual-prefs:ui:core"))
  }
}
