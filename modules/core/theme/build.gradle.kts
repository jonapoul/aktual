plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.core.theme"
}

dependencies {
  implementation(libs.androidx.coreKtx)
}
