plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules:domain"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
