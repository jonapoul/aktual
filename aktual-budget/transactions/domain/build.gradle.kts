plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:rules:domain"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
