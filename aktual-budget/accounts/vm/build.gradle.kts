plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:accounts:domain"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-budget:banksync:domain"))
  }
}
