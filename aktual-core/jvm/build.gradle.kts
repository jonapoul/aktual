plugins {
  id("aktual.module.jvm")
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(libs.compose.ui)
  api(libs.compose.uiUnit)
  api(libs.okio)
  api(project(":aktual-core"))
  api(project(":aktual-core:logging:impl"))
  api(project(":aktual-prefs"))
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.prefsCore)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.logcat)
  implementation(project(":aktual-di:core"))
  implementation(project(":aktual-prefs:impl"))
  testImplementation(kotlin("test"))
  testImplementation(project(":aktual-core:logging"))
}
