plugins {
  id("module-kotlin")
  kotlin("plugin.serialization")
}

dependencies {
  api(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
}
