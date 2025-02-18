plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.javaxInject)
  api(projects.api.github)
  api(projects.core.buildconfig)
  api(projects.core.log)
  implementation(libs.hilt.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.retrofit.core)
  implementation(projects.api.core)
  implementation(projects.url.model)
}
