plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-di:core"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
