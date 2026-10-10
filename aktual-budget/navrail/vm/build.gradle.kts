plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-budget"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-core:logging"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
