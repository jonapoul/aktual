plugins {
  alias(libs.plugins.module.viewmodel)
}

dependencies {
  api(libs.alakazam.android.core)
  api(libs.androidx.lifecycle.viewmodel.core)
  api(libs.dagger.core)
  api(libs.kotlinx.coroutines)
  api(projects.about.licenses.data)
  implementation(libs.alakazam.kotlin.logging)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.core)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.immutable)
  implementation(libs.molecule)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
  compileOnly(libs.hilt.core)
  testImplementation(projects.test.coroutines)
}
