plugins { id("aktual.module.android") }

dependencies {
  api(project(":aktual-core"))
  api(project(":aktual-core:logging:impl"))
  implementation(project(":aktual-di:core"))
  implementation(libs.androidx.core)
  implementation(libs.logcat)
  implementation(libs.okio)
  testImplementation(project(":aktual-core:logging"))
  testImplementation(kotlin("test-junit"))
}
