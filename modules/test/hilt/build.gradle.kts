plugins {
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.agp.lib)
  alias(libs.plugins.convention.android.library)
  alias(libs.plugins.convention.diagrams)
  alias(libs.plugins.convention.hilt)
  alias(libs.plugins.convention.style)
  alias(libs.plugins.dependencyAnalysis)
  alias(libs.plugins.dependencySort)
  alias(libs.plugins.hilt)
}

dependencies {
  api(libs.androidx.activity.core)
  api(libs.androidx.lifecycle.viewmodel.core)
  api(libs.hilt.android)
  api(libs.hilt.core)
  api(libs.test.androidx.runner)
  implementation(libs.androidx.annotation)
  implementation(libs.test.hilt)
}
