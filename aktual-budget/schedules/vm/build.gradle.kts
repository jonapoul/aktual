plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules:domain"))
    api(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-budget:transactions:domain"))
    implementation(project(":aktual-prefs:impl"))
  }
}
