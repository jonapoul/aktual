plugins {
  id("module-resources")
}

android {
  namespace = "dev.jonpoulton.actual.core.res"
}

dependencies {
  implementation(libs.androidx.splash)
  implementation(libs.material)
}
