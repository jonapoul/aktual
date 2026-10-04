plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:home:domain"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
