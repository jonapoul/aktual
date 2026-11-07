plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(project(":aktual-core:nav"))
    api(project(":aktual-crashes:vm"))
    implementation(libs.androidx.compose.annotation)
    implementation(project(":aktual-about:components"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
