plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:schedules"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:utils"))
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
