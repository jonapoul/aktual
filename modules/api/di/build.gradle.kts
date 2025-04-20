plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(projects.api.github)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.retrofit.core)
  implementation(projects.api.builder)
  implementation(projects.url.model)
}
