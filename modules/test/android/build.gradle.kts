plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(project(":modules:prefs"))
  api(libs.preferences.core)
  implementation(libs.preferences.android)
  implementation(libs.test.androidx.coreKtx)
}
