plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.api.github)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.core)
  implementation(projects.api.builder)
}
