plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.connection)
  api(projects.login.prefs)
  api(projects.serverUrl.prefs)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.android)
  testImplementation(projects.test.http)
}
