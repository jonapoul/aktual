plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.api.client"
}

dependencies {
  api(projects.modules.api.model)
  implementation(libs.okhttp)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
}
