plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.preferences.core)
  implementation(libs.preferences.android)
  implementation(libs.test.androidx.coreKtx)
}
