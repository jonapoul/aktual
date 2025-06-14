plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(projects.modules.account.login.domain)
  api(projects.modules.account.model)
  api(projects.modules.core.model)
  api(projects.modules.prefs)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.preferences.core)
  implementation(projects.modules.account.login.domain)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.modules.core.connection)
}
