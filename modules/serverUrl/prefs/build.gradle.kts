plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
  testImplementation(projects.test.android)
  testImplementation(projects.test.prefs)
}
