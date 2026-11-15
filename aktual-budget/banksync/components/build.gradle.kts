plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:impl"))
    implementation(libs.compose.resources)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
