plugins { id("aktual.module.android") }

dependencies {
  api(libs.androidx.test.core)
  implementation(project(":aktual-prefs:impl"))
  implementation(project(":aktual-test"))
}
