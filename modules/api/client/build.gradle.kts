plugins {
  alias(libs.plugins.module.kotlin)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.retrofit.core)
  api(projects.api.model)
}
