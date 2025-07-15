plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:budget:data"))
  api(project(":modules:budget:model"))
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:budget:di"))
  implementation(project(":modules:prefs"))
  implementation(libs.androidx.compose.runtime)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(libs.sqldelight.driver.android)
}
