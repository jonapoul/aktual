plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.preferences.core)
  implementation(projects.api.client)
  implementation(projects.core.connection)
  implementation(projects.serverUrl.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(projects.test.android)
  testImplementation(projects.test.prefs)
}
