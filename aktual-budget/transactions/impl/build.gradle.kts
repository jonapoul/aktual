plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:rules:impl"))
    api(project(":aktual-budget:transactions"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
