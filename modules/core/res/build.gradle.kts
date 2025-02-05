plugins {
  alias(libs.plugins.module.resources)
}

dependencies {
  api(libs.androidx.compose.runtime)
  implementation(libs.androidx.splash)
  implementation(libs.material)
}
