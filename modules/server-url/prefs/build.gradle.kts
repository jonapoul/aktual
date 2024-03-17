plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.prefs"
}

dependencies {
  api(projects.modules.serverUrl.model)
  implementation(libs.flowpreferences)
  implementation(libs.javaxInject)
}
