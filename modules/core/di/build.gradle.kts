plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.androidx.preference.ktx)
  api(libs.hilt.android)
  api(libs.javaxInject)
  api(libs.kotlinx.datetime)
  api(libs.preferences.core)
  api(projects.core.files)
  api(projects.core.model)
  implementation(libs.preferences.android)
}
