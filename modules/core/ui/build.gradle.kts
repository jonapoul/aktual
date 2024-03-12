plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.core.ui"
}

dependencies {
  implementation(libs.androidx.coreKtx)
}
