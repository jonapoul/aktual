plugins {
  id("module-android")
  id("convention-hilt")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.vm"
}

dependencies {
  api(libs.androidx.lifecycle.viewmodel.ktx)
  api(libs.dagger.core)
  api(libs.javax.inject)
  api(libs.kotlinx.coroutines)
  implementation(libs.alakazam.android.core)
  implementation(libs.hilt.android)
}
