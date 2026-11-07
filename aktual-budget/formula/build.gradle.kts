plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:utils"))
    implementation(libs.kotlinx.datetime)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
