plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:logging:format"))
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
