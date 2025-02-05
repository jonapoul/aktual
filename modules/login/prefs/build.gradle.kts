plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
  api(projects.core.model)
}
