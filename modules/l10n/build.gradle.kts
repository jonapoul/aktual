plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.convention.compose)
}

android {
  androidResources.enable = true
  buildFeatures.resValues = true
}

compose.resources {
  generateResClass = always
  nameOfResClass = "Res"
  packageOfResClass = "actual.l10n"
  publicResClass = true
}

kotlin {
  commonMainDependencies {
    api(compose.components.resources)
    api(compose.runtime)
  }
}
