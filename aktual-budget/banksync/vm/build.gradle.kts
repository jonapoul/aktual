plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:domain"))
    implementation(project(":aktual-api"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
