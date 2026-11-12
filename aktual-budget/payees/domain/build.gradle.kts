plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-budget:data:db"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:utils"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
