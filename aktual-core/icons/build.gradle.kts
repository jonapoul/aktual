plugins {
  id("aktual.module.kotlin")
  id("aktual.convention.compose")
}

compose.resources {
  generateResClass = always
  packageOfResClass = "aktual.core.icons.flags"
}

kotlin {
  android { androidResources.enable = true }

  commonMainDependencies {
    api(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.resources)
    implementation(libs.compose.uiToolingPreview)
  }
}
