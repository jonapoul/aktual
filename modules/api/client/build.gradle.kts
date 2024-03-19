plugins {
  id("module-android")
}

android {
  namespace = "dev.jonpoulton.actual.api.client"
}

dependencies {
  api(projects.modules.api.model)
  api(libs.alakazam.kotlin.core)
  api(libs.retrofit.core)
  implementation(projects.modules.core.model)
  implementation(libs.javaxInject)
}
