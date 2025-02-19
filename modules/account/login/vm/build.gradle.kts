plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(projects.account.model)
  api(projects.core.colorscheme)
  api(projects.core.coroutines)
  api(projects.core.log)
  api(projects.url.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.login.prefs)
  implementation(projects.api.actual)
  implementation(projects.core.versions)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.core.connection)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
