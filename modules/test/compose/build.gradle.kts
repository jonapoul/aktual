plugins {
  alias(libs.plugins.module.compose)
}

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.ui.text)
  api(libs.test.androidx.compose.ui.core)
  api(libs.test.androidx.compose.ui.junit4)
  api(libs.test.kotlinx.coroutines)
  api(projects.core.ui)
  implementation(libs.androidx.compose.ui.core)
}
