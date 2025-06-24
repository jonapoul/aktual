plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.alakazam.kotlin.time)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.immutable)
  api(projects.modules.budget.di)
  api(projects.modules.budget.model)
  api(projects.modules.core.model)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.ktor.core)
  implementation(libs.okio)
  implementation(projects.modules.account.model)
  implementation(projects.modules.api.actual)
  implementation(projects.modules.budget.encryption)
  implementation(projects.modules.prefs)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
