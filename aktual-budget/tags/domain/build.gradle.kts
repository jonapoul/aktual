plugins {
  id("aktual.module.kmp-android")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    compileOnly(libs.compose.runtime)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
