plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-prefs"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
