plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.core.connection"
}

dependencies {
  api(projects.modules.api.client)
  implementation(projects.modules.api.json)
  implementation(projects.modules.core.model)
  implementation(projects.modules.serverUrl.prefs)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.flowpreferences)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)
}
