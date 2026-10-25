plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:budgeting:domain"))
    api(project(":aktual-budget:reports:vm"))
    api(project(":aktual-budget:schedules:domain"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
  }
}
