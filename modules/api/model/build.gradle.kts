plugins {
  id("module-kotlin")
  kotlin("plugin.serialization")
  alias(libs.plugins.sekret)
}

dependencies {
  api(projects.modules.core.model)
  api(libs.kotlinx.serialization.core)
  implementation(libs.alakazam.kotlin.serialization)
  implementation(libs.kotlinx.serialization.json)
  compileOnly(libs.sekret)
  testImplementation(projects.modules.api.json)
}
