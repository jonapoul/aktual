plugins {
  alias(libs.plugins.module.kotlin)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.sekret)
}

dependencies {
  api(libs.alakazam.kotlin.serialization)
  api(libs.kotlinx.serialization.core)
  api(projects.login.model)
  implementation(libs.kotlinx.serialization.json)
  compileOnly(libs.sekret)
  testImplementation(projects.api.json)
}
