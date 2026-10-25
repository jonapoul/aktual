plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules:domain"))
    api(project(":aktual-prefs"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
