plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:rules"))
    compileOnly(libs.androidx.compose.annotation)
  }
}
