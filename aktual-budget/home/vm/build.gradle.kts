plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies { api(project(":aktual-budget")) }

  androidHostTestDependencies { implementation(project(":aktual-test")) }
}
