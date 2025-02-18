plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.colorscheme)
  api(projects.core.coroutines)
  api(projects.core.log)
  api(projects.url.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.api.actual)
  implementation(projects.core.versions)
  implementation(projects.login.model)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
}
