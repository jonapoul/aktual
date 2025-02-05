plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.core.connection)
  implementation(projects.login.prefs)
  implementation(projects.serverUrl.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(projects.test.android)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
