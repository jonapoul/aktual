plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:impl"))
    api(project(":aktual-budget:schedules:impl"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
