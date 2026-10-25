plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:transactions:domain"))
    implementation(libs.metrox.viewmodel)
  }
}
