plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.preferences.core)
  api(projects.login.model)
  implementation(libs.javaxInject)
}
