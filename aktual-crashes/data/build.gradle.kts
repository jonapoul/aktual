plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.okio)
    api(project(":aktual-logging:format"))
    implementation(libs.ktor.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
