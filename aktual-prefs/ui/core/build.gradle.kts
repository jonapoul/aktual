plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(project(":aktual-core:ui"))
  }
}
