plugins {
  id("module-kotlin")
  kotlin("plugin.serialization")
  alias(libs.plugins.sekret)
}

dependencies {
  api(libs.kotlinx.serialization.core)
  implementation(libs.alakazam.kotlin.serialization)
  implementation(libs.kotlinx.serialization.json)
  compileOnly(libs.sekret)
  testImplementation(projects.modules.api.json)
}
