plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.api.client)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
  implementation(projects.api.json)
  implementation(projects.core.coroutines)
  implementation(projects.core.model)
  implementation(projects.core.state)
  implementation(projects.serverUrl.prefs)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(libs.alakazam.android.core)
  testImplementation(projects.test.android)
  testImplementation(projects.test.prefs)
}
