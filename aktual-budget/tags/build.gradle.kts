plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(project(":aktual-budget:data:db"))
    compileOnly(libs.compose.runtime)
  }
}
