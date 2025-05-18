plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.prefs)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.molecule)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.prefs)
}
