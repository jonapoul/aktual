plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.ui)
    api(libs.kotlinx.coroutines.core)
    api(project(":aktual-about:model"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-core:l10n"))
  }
}
