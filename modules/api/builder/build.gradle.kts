import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.kotlinx.serialization.json)
  api(libs.okhttp.core)
  api(libs.retrofit.core)
  api(projects.url.model)
  implementation(libs.okhttp.logging)
  implementation(libs.retrofit.scalars)
  implementation(libs.retrofit.serialization)
}
