plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:payees:impl"))
    api(project(":aktual-budget:rules:impl"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
