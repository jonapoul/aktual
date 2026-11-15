plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:tags"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
