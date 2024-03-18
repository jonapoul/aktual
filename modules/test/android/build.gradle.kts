plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.test.android"
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.flowpreferences)
  api(libs.kotlinx.datetime)
  implementation(libs.test.androidx.coreKtx)
}
