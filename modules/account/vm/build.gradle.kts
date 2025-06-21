plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(projects.modules.account.domain)
  api(projects.modules.account.model)
  api(projects.modules.core.model)
  api(projects.modules.prefs)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.hilt.android)
  implementation(libs.ktor.core)
  implementation(libs.preferences.core)
  implementation(projects.modules.api.actual)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.modules.core.connection)
}
