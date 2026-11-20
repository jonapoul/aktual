plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:data:db"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:banksync:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
  }
}
