plugins {
  id("module-compose")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.ui"
}

optIn(className = "androidx.compose.material3.ExperimentalMaterial3Api")

dependencies {
  api(projects.modules.serverUrl.vm)
  api(libs.androidx.compose.foundation.layout)
  api(libs.androidx.compose.runtime)
  api(libs.androidx.navigation.runtime)
  implementation(projects.modules.core.res)
  implementation(projects.modules.core.ui)
  implementation(libs.alakazam.android.compose)
  implementation(libs.androidx.compose.animation.core)
  implementation(libs.androidx.compose.foundation.core)
  implementation(libs.androidx.compose.hilt)
  implementation(libs.androidx.compose.ui.core)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.text)
  implementation(libs.androidx.compose.ui.unit)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.viewmodel.core)
  implementation(libs.kotlinx.coroutines)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
