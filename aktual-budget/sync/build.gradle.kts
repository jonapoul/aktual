plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-budget:utils"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:data:proto"))
    implementation(project(":aktual-logging"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
