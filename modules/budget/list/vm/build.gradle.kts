plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.preferences.core)
  implementation(projects.core.connection)
  implementation(projects.login.prefs)
  implementation(projects.serverUrl.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.android)
  testImplementation(projects.test.http)
}
