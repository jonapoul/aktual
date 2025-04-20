plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.hilt.android)
  api(libs.javaxInject)
  api(libs.preferences.core)
  api(projects.core.files)
  implementation(libs.preferences.android)
}
