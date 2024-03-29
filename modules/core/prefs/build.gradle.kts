plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.core.prefs"
}

dependencies {
  api(libs.flowpreferences)
}
