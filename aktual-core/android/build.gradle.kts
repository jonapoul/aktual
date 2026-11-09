plugins { id("aktual.module.android") }

dependencies {
  api(project(":aktual-core:logging:impl"))
  api(project(":aktual-core:utils"))
  implementation(project(":aktual-core:logging"))
  implementation(project(":aktual-di:core"))
  implementation(libs.androidx.core)
  implementation(libs.logcat)
  implementation(libs.okio)
  testImplementation(kotlin("test-junit"))
}
