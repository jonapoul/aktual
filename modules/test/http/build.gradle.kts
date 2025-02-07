plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.test.junit)
  api(libs.test.okhttp)
  api(projects.url.model)
  implementation(libs.okhttp.core)
}
