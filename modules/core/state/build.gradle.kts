plugins {
  alias(libs.plugins.module.android)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.core.model)
  implementation(libs.kotlinx.coroutines)
}
