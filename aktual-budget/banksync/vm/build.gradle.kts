plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:banksync:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
  }
}
