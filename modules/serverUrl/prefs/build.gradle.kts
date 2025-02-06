plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
  api(projects.core.model)
  testImplementation(projects.test.android)
  testImplementation(projects.test.prefs)
}
