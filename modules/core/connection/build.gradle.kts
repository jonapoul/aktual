plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.javaxInject)
  api(projects.api.client)
  api(projects.api.json)
  api(projects.core.coroutines)
  api(projects.core.versions)
  api(projects.url.model)
  api(projects.url.prefs)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
  implementation(projects.core.buildconfig)
  testImplementation(libs.alakazam.android.core)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.prefs)
}
