plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(projects.account.model)
  api(projects.account.password.domain)
  api(projects.core.versions)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(projects.account.login.domain)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.core.connection)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
  testImplementation(projects.test.prefs)
}
