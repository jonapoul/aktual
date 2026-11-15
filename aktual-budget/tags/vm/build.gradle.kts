plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(libs.compose.uiGraphics)
    api(project(":aktual-budget:tags"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-prefs"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.metrox.viewmodel)
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:tags:impl"))
    implementation(project(":aktual-prefs:impl"))
  }
}
