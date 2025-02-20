import actual.gradle.androidUnitTestDependencies
import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
  api(projects.account.model)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.preferences.core)
  implementation(libs.retrofit.core)
  implementation(projects.account.login.prefs)
  implementation(projects.api.actual)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}

androidUnitTestDependencies {
  implementation(projects.core.connection)
  implementation(projects.test.buildconfig)
  implementation(projects.test.coroutines)
  implementation(projects.test.http)
  implementation(projects.test.prefs)
  implementation(projects.url.prefs)
}
