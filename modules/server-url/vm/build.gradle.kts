plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.vm"
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(libs.okhttp.core)
  implementation(projects.modules.api.client)
  implementation(projects.modules.serverUrl.model)
  implementation(projects.modules.serverUrl.prefs)
  implementation(libs.alakazam.android.core)
  implementation(libs.hilt.android)
  implementation(libs.retrofit.core)
  implementation(libs.timber)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
