import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.kotlinx.serialization.core)
  api(libs.okhttp.core)
  api(libs.retrofit.core)
}
