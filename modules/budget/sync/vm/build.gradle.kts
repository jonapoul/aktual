plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(projects.core.colorscheme)
  api(projects.core.files)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.okhttp.core)
  implementation(libs.okio)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.model)
  implementation(projects.api.actual)
  implementation(projects.budget.model)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
}
