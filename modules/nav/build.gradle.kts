plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.nav"
}

dependencies {
  api(projects.modules.listBudgets.ui)
  api(projects.modules.login.ui)
  api(projects.modules.serverUrl.ui)
  api(libs.androidx.compose.runtime)
  api(libs.androidx.navigation.runtime)
  implementation(projects.modules.login.vm)
  implementation(libs.androidx.compose.animation.full)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.navigation.commonKtx)
  implementation(libs.androidx.navigation.compose)
}
