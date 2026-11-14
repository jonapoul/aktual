plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:rules:api"))
    compileOnly(libs.androidx.compose.annotation)
  }
}
