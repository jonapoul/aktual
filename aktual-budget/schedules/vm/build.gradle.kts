plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules:impl"))
    api(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
    implementation(project(":aktual-prefs:impl"))
  }
}
