plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.modules.prefs)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.molecule)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
