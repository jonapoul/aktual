plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(project(":aktual-prefs"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:ui"))
  }
}
