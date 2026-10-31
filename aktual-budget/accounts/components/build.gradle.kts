plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
  }
}
