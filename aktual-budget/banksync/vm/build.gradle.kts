plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:impl"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:transactions:impl"))
  }
}
