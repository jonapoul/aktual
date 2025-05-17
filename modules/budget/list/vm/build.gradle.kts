plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.budget.model)
  api(projects.core.model)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.ktor.core)
  implementation(libs.ktor.serialization.core)
  implementation(libs.preferences.core)
  implementation(projects.account.model)
  implementation(projects.api.actual)
  implementation(projects.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
  testImplementation(projects.test.http)
}
