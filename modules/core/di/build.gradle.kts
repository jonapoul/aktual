plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.androidx.preference.ktx)
  api(libs.hilt.android)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.okio)
  api(libs.preferences.core)
  api(projects.core.files)
  api(projects.core.model)
  implementation(libs.preferences.android)
}

ksp {
  // https://dagger.dev/hilt/flags#disable-install-in-check
  // Done so we can include modules as-needed for instrumented tests, then use @InstallIn at the app level
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}
