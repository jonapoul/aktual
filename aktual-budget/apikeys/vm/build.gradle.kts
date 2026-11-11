plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    implementation(project(":aktual-budget"))
    implementation(project(":aktual-core:utils"))
  }
}
