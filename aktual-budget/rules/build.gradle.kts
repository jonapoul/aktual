plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-logging"))
  }
}
