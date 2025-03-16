plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.alakazam.kotlin.core)
  api(libs.kotlinx.coroutines)
  api(projects.about.info.data)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.datetime)
  implementation(libs.molecule)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  compileOnly(libs.hilt.core)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
}
