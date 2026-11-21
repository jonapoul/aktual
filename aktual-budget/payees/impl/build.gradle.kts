plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:payees"))
    api(project(":aktual-budget:utils"))
    api(project(":openstreetmap-api"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-logging"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
