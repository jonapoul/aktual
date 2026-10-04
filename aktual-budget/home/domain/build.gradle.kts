plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:schedules:domain"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
