plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:impl"))
    api(project(":aktual-budget:budgeting:impl"))
    api(project(":aktual-budget:reports"))
    api(project(":aktual-budget:schedules:impl"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
  }
}
