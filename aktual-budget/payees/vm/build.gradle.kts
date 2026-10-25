plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:payees:domain"))
    api(project(":aktual-budget:rules:domain"))
    implementation(libs.metrox.viewmodel)
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
