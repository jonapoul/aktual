plugins {
  id("module-android")
  id("convention-hilt")
}

android {
  namespace = "dev.jonpoulton.actual.login.vm"
}

dependencies {
  api(libs.androidx.lifecycle.viewmodel.ktx)
  api(libs.dagger.core)
  api(libs.javax.inject)
  implementation(libs.hilt.android)
}
