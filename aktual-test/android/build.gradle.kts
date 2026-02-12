plugins { id("aktual.module.android") }

dependencies {
  api(project(":aktual-core:prefs-impl"))
  api(libs.preferences.core)
  implementation(libs.preferences.android)
  implementation(libs.test.androidx.coreKtx)
}
