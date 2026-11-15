plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:budgeting"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:utils"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-budget:budgeting:impl"))
  }
}
