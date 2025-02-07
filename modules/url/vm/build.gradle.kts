plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.connection)
  api(projects.url.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(testFixtures(projects.core.coroutines))
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.prefs)
}
