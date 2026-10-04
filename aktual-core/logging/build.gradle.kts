plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
