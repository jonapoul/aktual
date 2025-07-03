plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(project(":modules:budget:model"))
  api(project(":modules:core:model"))
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.ktor.core)
  implementation(libs.ktor.serialization.core)
  implementation(libs.okio)
  implementation(libs.preferences.core)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:api:actual"))
  implementation(project(":modules:prefs"))
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
