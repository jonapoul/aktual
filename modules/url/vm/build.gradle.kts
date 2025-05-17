plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.kotlinx.coroutines)
  api(projects.core.model)
  api(projects.core.versions)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.javaxInject)
  implementation(libs.ktor.core)
  implementation(libs.molecule)
  implementation(libs.preferences.core)
  implementation(projects.account.login.domain)
  implementation(projects.api.actual)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
