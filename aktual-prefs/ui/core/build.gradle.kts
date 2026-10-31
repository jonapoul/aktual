plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:ui"))
  }
}
