plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  api(projects.modules.budget.data)
  api(projects.modules.budget.model)
  api(projects.modules.core.model)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.molecule)
  implementation(projects.modules.account.model)
  implementation(projects.modules.budget.di)
  implementation(projects.modules.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(libs.sqldelight.driver.android)
}
