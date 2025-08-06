plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:budget:model"))
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:budget:di"))
  implementation(project(":modules:core:model"))
  implementation(compose.runtime)
  testImplementation(libs.sqldelight.coroutines)
  testImplementation(libs.sqldelight.driver.android)
}
