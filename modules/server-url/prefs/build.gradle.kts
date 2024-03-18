plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.prefs"
}

dependencies {
  api(projects.modules.core.model)
  api(libs.flowpreferences)
  implementation(libs.javaxInject)
  testImplementation(projects.modules.test.android)
}
