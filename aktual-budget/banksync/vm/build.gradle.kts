plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:domain"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
