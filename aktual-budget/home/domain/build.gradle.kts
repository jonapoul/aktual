plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
