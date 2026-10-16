plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    implementation(libs.kotlinx.datetime)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
