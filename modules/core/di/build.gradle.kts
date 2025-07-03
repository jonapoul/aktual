plugins {
  alias(libs.plugins.module.hilt)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.alakazam.kotlin.time)
  api(libs.androidx.preference.ktx)
  api(libs.hilt.android)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.okio)
  api(libs.preferences.core)
  api(project(":modules:api:github"))
  api(project(":modules:budget:model"))
  api(project(":modules:core:model"))
  api(project(":modules:prefs"))
  implementation(libs.androidx.crypto)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.core)
  implementation(libs.preferences.android)
  implementation(project(":modules:api:builder"))
}

ksp {
  // https://dagger.dev/hilt/flags#disable-install-in-check
  // Done so we can include modules as-needed for instrumented tests, then use @InstallIn at the app level
  arg("dagger.hilt.disableModulesHaveInstallInCheck", "true")
}
