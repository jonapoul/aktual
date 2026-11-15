plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:budgeting:impl"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
  }
}
