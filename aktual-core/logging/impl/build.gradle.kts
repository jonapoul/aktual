plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:logging"))
    api(project(":aktual-di:core"))
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
