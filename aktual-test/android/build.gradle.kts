plugins { id("aktual.module.android") }

dependencies {
  api(libs.androidx.test.core)
  api(project(":aktual-prefs:impl"))
  api(project(":aktual-test"))
  implementation(project(":aktual-di:core"))
}
