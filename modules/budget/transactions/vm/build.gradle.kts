plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  api(project(":modules:budget:data"))
  api(project(":modules:budget:model"))
  implementation(libs.androidx.compose.runtime)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:budget:di"))
  implementation(project(":modules:prefs"))
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(libs.sqldelight.driver.android)
}
