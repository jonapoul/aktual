plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(projects.core.colorscheme)
  api(projects.core.coroutines)
  api(projects.core.log)
  api(projects.login.model)
  api(projects.url.model)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.api.actual)
  implementation(projects.core.versions)
  implementation(projects.login.prefs)
  implementation(projects.url.prefs)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
