plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-core:l10n"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
  }
}
