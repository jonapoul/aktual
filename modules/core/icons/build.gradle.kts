plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.core.icons"
}

dependencies {
  api(libs.androidx.compose.runtime)
  implementation(projects.modules.core.ui)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.toolingPreview)
  implementation(libs.androidx.compose.ui.unit)
  implementation(libs.androidx.compose.material3)
}
