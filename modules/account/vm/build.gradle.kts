plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:account:domain"))
  api(project(":modules:account:model"))
  api(project(":modules:core:model"))
  api(project(":modules:prefs"))
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  implementation(project(":modules:api:actual"))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.ktor.core)
  implementation(libs.preferences.core)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(project(":modules:core:connection"))
}
