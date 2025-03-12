plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
<<<<<<< HEAD
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.colorscheme)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.molecule)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.login.domain)
  implementation(projects.api.actual)
  implementation(projects.core.versions)
  implementation(projects.url.prefs)
||||||| parent of 79b5423 (Added settings screen)
=======
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.colorscheme)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.molecule)
  implementation(libs.preferences.core)
>>>>>>> 79b5423 (Added settings screen)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.prefs)
}
