plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.api.client"
}

dependencies {
  api(projects.modules.api.model)
  api(libs.alakazam.kotlin.core)
  api(libs.okhttp.core)
  api(libs.retrofit.core)
  implementation(projects.modules.api.json)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.logging)
  implementation(libs.retrofit.serialization)
  implementation(libs.timber)
}
