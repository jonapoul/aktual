plugins {
  id("module-viewmodel")
}

android {
  namespace = "dev.jonpoulton.actual.login.vm"
}

dependencies {
  api(projects.modules.core.model)
  api(libs.kotlinx.coroutines)
  implementation(projects.modules.core.connection)
  implementation(projects.modules.core.coroutines)
  implementation(libs.alakazam.android.core)
}
