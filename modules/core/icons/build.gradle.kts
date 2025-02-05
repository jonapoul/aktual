plugins {
  alias(libs.plugins.module.compose)
}

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.ui.toolingPreview)
  implementation(libs.androidx.compose.ui.unit)
  implementation(projects.core.ui)
}
