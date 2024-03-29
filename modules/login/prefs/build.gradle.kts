plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.login.prefs"
}

dependencies {
  api(projects.modules.core.model)
  api(libs.flowpreferences)
  implementation(projects.modules.core.prefs)
  implementation(libs.javaxInject)
  testImplementation(projects.modules.test.android)
}
