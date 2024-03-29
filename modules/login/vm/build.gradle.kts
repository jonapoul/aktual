plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.login.vm"
}

dependencies {
  api(projects.modules.core.model)
  api(libs.kotlinx.coroutines)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  implementation(projects.modules.core.connection)
  implementation(projects.modules.core.coroutines)
  implementation(projects.modules.login.prefs)
  implementation(projects.modules.serverUrl.prefs)
  implementation(libs.alakazam.android.core)
  testImplementation(projects.modules.test.android)
}
