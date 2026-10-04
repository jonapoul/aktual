plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
