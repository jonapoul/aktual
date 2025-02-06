plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.api.client)
  api(projects.api.json)
  api(projects.core.coroutines)
  api(projects.core.state)
  api(projects.serverUrl.prefs)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(libs.alakazam.android.core)
  testImplementation(projects.test.android)
  testImplementation(projects.test.prefs)
}
