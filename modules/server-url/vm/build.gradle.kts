plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.serverurl.vm"
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  implementation(projects.modules.api.client)
  implementation(projects.modules.core.connection)
  implementation(projects.modules.core.coroutines)
  implementation(projects.modules.core.model)
  implementation(projects.modules.core.state)
  implementation(projects.modules.serverUrl.prefs)
  implementation(libs.alakazam.android.core)
  implementation(libs.flowpreferences)
  implementation(libs.hilt.android)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.modules.test.android)
}
