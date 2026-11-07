plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(project(":aktual-core:nav"))
    api(project(":aktual-crashes:vm"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
