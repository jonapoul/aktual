plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:domain"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:transactions:domain"))
  }
}
