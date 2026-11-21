plugins { id("aktual.module.android") }

dependencies {
  implementation(project(":aktual-di:core"))
  implementation(project(":aktual-logging"))
  implementation(project(":aktual-logging:impl"))
  implementation(project(":aktual-prefs"))
  implementation(libs.androidx.core)
  implementation(libs.logcat)
  implementation(libs.okio)
  testImplementation(kotlin("test-junit"))
}
