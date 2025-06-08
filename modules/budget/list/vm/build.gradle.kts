plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.immutable)
  api(projects.modules.budget.model)
  api(projects.modules.core.model)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.ktor.core)
  implementation(libs.ktor.serialization.core)
  implementation(libs.preferences.core)
  implementation(projects.modules.account.model)
  implementation(projects.modules.api.actual)
  implementation(projects.modules.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(projects.modules.test.buildconfig)
  testImplementation(projects.modules.test.coroutines)
  testImplementation(projects.modules.test.http)
  testImplementation(projects.modules.test.prefs)
}
