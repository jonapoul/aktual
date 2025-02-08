plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.hilt.android)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.preferences.core)
  api(projects.core.coroutines)
  api(projects.core.log)
  implementation(libs.androidx.preference.ktx)
  implementation(libs.preferences.android)
  implementation(libs.timber)
}
