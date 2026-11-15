plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:payees"))
    api(project(":aktual-budget:rules"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:payees:impl"))
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-prefs:impl"))
  }
}
