plugins {
  alias(libs.plugins.module.android)
}

android {
  androidResources.enable = true
  buildFeatures.resValues = true
}

dependencies {
  implementation(libs.androidx.splash)
  implementation(libs.material)
}
