plugins { id("aktual.module.android") }

dependencies {
  api(project(":aktual-core"))
  api(project(":aktual-core:logging:impl"))
  implementation(libs.androidx.core)
  implementation(libs.logcat)
  implementation(libs.okio)
  implementation(project(":aktual-di:core"))
  testImplementation(kotlin("test-junit"))
  testImplementation(project(":aktual-core:logging"))
}
