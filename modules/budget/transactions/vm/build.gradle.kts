plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  api(projects.modules.budget.data)
  api(projects.modules.budget.model)
  implementation(libs.androidx.compose.runtime)
  implementation(projects.modules.account.model)
  implementation(projects.modules.budget.di)
  implementation(projects.modules.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  testImplementation(libs.sqldelight.driver.android)
}
