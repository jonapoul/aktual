plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.vm"
}

dependencies {
  api(libs.kotlinx.coroutines)
  implementation(libs.alakazam.android.core)
}
