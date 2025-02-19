plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(projects.core.colorscheme)
  api(projects.core.coroutines)
  api(projects.core.log)
  api(projects.url.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.preferences.core)
  implementation(projects.account.login.prefs)
  implementation(projects.api.actual)
  implementation(projects.core.versions)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.prefs)
}
