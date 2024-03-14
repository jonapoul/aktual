plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.core.ui"
}

dependencies {
  api(projects.modules.core.res)
  api(libs.androidx.compose.runtime)
  implementation(libs.alakazam.android.compose)
  implementation(libs.androidx.coreKtx)
  implementation(libs.androidx.compose.foundation.core)
  implementation(libs.androidx.compose.foundation.layout)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text)
  implementation(libs.androidx.compose.ui.unit)
  implementation(libs.androidx.compose.material3)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
