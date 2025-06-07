plugins {
  alias(libs.plugins.module.resources)
}

catalog {
  typePrefix = "Core"
}

dependencies {
  implementation(libs.androidx.compose.ui.unit)
  implementation(libs.androidx.splash)
  implementation(libs.material)
}
