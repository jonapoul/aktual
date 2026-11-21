plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:vm"))
    api(project(":aktual-core:nav"))
    implementation(project(":aktual-core:icons"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
    implementation(project(":aktual-logging"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
