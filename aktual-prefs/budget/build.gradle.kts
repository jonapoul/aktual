plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-prefs"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:utils"))
    implementation(project(":aktual-di:core"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
