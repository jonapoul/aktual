plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:prefs"))
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
