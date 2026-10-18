plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:logging"))
    api(project(":aktual-di:core"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
