plugins {
  id("aktual.module.jvm")
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(project(":aktual-logging:impl"))
  api(project(":aktual-prefs"))
  api(libs.compose.ui)
  api(libs.compose.uiUnit)
  api(libs.okio)
  implementation(project(":aktual-core:l10n"))
  implementation(project(":aktual-di:core"))
  implementation(project(":aktual-prefs:impl"))
  implementation(libs.androidx.datastore.core)
  implementation(libs.androidx.datastore.prefsCore)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.logcat)
  testImplementation(project(":aktual-logging"))
  testImplementation(kotlin("test"))
}
