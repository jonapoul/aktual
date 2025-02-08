plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
  api(projects.about.data)
  api(projects.core.buildconfig)
  api(projects.core.log)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.kotlinx.datetime)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  compileOnly(libs.hilt.core)
  testImplementation(projects.test.buildconfig)
  testImplementation(projects.test.coroutines)
}
