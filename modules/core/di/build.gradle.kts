plugins {
  id("module-android")
  id("convention-hilt")
  id("convention-desugaring")
}

android {
  namespace = "dev.jonpoulton.actual.core.di"
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.dagger.core)
  api(libs.kotlinx.datetime)
  implementation(libs.hilt.core)
  implementation(libs.javax.inject)
  implementation(libs.kotlin.stdlib)
  implementation(libs.kotlinx.coroutines)
}
