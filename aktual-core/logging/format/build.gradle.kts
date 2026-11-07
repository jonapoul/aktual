plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:model"))
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
