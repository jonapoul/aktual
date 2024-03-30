plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.core.state"
}

dependencies {
  api(projects.modules.core.model)
  api(libs.alakazam.kotlin.core)
  implementation(libs.alakazam.android.core)
  implementation(libs.javaxInject)
}
