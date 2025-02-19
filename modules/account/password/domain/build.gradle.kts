import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
  api(projects.account.login.domain)
  api(projects.account.model)
  api(projects.core.coroutines)
  api(projects.core.log)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.login.prefs)
  implementation(projects.api.actual)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
