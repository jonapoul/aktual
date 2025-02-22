plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.core.colorscheme)
  api(projects.url.model)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.okhttp.core)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.model)
  implementation(projects.api.actual)
  implementation(projects.api.builder)
  implementation(projects.budget.model)
  implementation(projects.core.versions)
  implementation(projects.url.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
}
