plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(project(":modules:budget:model"))
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  implementation(project(":modules:account:model"))
  implementation(project(":modules:core:model"))
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
}
