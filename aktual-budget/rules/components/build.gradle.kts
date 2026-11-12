plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
