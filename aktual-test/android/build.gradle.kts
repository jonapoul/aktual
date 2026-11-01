plugins { id("aktual.module.android") }

dependencies {
  api(project(":aktual-prefs:impl"))
  api(project(":aktual-test"))
  api(libs.androidx.test.core)
  implementation(project(":aktual-di:core"))
}
