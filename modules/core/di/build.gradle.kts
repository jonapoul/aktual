plugins {
  id("module-android")
  id("convention-hilt")
  id("convention-desugaring")
}

android {
  namespace = "dev.jonpoulton.actual.core.di"
}

dependencies {
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.kotlinx.datetime)
}
