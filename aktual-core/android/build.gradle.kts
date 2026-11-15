plugins { id("aktual.module.android") }

dependencies {
  implementation(project(":aktual-core:logging"))
  implementation(project(":aktual-core:logging:impl"))
  implementation(project(":aktual-core:utils"))
  implementation(project(":aktual-di:core"))
  implementation(project(":aktual-prefs"))
  implementation(libs.androidx.core)
  implementation(libs.logcat)
  implementation(libs.okio)
  testImplementation(kotlin("test-junit"))
}
