plugins {
  alias(libs.plugins.module.compose)
}

dependencies {
  api(libs.androidx.compose.runtime)
  api(libs.androidx.navigation.runtime)
  api(projects.budget.list.ui)
  api(projects.login.ui)
  api(projects.url.ui)
  implementation(libs.androidx.compose.animation.full)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.navigation.commonKtx)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.coroutines)
}
