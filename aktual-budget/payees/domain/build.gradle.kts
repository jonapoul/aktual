plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
