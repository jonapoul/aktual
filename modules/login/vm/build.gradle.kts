plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(projects.core.colorscheme)
  api(projects.core.connection)
  api(projects.login.model)
  api(projects.url.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.api.client)
  implementation(projects.api.model)
  implementation(projects.core.coroutines)
  implementation(projects.core.log)
  implementation(projects.core.versions)
  implementation(projects.login.prefs)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
