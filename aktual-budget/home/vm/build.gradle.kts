plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:budgeting:domain"))
    api(project(":aktual-budget:schedules:domain"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }

  androidHostTestDependencies {
    implementation(libs.sqldelight.runtime)
  }
}
