plugins {
  alias(libs.plugins.module.android)
}

android {
  buildFeatures {
    androidResources = true
    resValues = true
  }
}

dependencies {
  implementation(libs.androidx.splash)
  implementation(libs.material)
}
