plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-prefs"))
    api(project(":aktual-theme"))
    implementation(project(":aktual-budget:transactions"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-di:runlevel"))
    implementation(project(":aktual-updates"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
  }
}
