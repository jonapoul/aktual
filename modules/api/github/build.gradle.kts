import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

commonMainDependencies {
  api(libs.kotlinx.datetime)
  api(libs.kotlinx.serialization.core)
  api(libs.retrofit.core)
  implementation(libs.kotlinx.serialization.json)
}
