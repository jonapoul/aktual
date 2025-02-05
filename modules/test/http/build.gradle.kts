plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.test.junit)
  api(libs.test.okhttp)
  api(projects.core.model)
  implementation(libs.okhttp.core)
}
