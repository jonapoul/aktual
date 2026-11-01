plugins {
  id("aktual.module.kmp-android")
  id("aktual.convention.compose")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
  }
}
