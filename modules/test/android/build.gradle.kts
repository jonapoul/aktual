plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.preferences.core)
  api(project(":modules:prefs"))
  implementation(libs.preferences.android)
  implementation(libs.test.androidx.coreKtx)
}
