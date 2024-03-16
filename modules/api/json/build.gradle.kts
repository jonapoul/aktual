plugins {
  id("module-kotlin")
}

dependencies {
  api(libs.kotlinx.serialization.json)
  testImplementation(projects.modules.api.json)
}
