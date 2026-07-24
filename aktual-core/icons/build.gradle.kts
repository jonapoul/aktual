import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
  id("aktual.convention.compose")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
  }
}
