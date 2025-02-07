plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
  api(projects.url.model)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.prefs)
}
