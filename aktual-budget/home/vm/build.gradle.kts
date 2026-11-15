plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:budgeting"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:reports"))
    api(project(":aktual-budget:schedules"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-budget:budgeting:impl"))
    implementation(project(":aktual-budget:schedules:impl"))
  }
}
