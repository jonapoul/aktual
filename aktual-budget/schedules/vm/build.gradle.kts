plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:utils"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:schedules:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
    implementation(project(":aktual-prefs:impl"))
  }
}
