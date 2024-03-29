plugins {
  id("module-kotlin")
}

dependencies {
  api(projects.modules.core.model)
  api(libs.test.okhttp)
  implementation(libs.okhttp.core)
}
