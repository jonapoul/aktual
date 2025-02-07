plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.colorscheme)
  api(projects.core.connection)
  api(projects.url.model)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.preferences.core)
  implementation(projects.core.versions)
  implementation(projects.login.prefs)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.http)
}
