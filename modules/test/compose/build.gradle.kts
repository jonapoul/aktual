plugins {
  alias(libs.plugins.module.compose)
}

dependencies {
  api(libs.test.androidx.compose.ui.junit4)
  api(libs.test.kotlinx.coroutines)
  api(projects.core.ui)
}
