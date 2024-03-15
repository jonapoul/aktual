plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.vm"
}

dependencies {
  api(libs.kotlinx.coroutines)
  implementation(libs.alakazam.android.core)
  implementation(libs.alakazam.kotlin.compose.annotations)
  implementation(libs.hilt.android)
  implementation(libs.timber)
}
