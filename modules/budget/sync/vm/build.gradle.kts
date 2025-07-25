plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:budget:di"))
  api(project(":modules:budget:model"))
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.alakazam.kotlin.time)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:api:actual"))
  implementation(project(":modules:budget:encryption"))
  implementation(project(":modules:core:model"))
  implementation(project(":modules:prefs"))
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.core)
  implementation(libs.okio)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
