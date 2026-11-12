plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    implementation(project(":aktual-budget:utils"))
    implementation(project(":aktual-core:utils"))
  }
}
