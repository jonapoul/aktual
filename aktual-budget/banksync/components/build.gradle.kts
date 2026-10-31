plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:domain"))
    implementation(libs.compose.resources)
    implementation(project(":aktual-core:l10n"))
  }
}
