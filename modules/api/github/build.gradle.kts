import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
    api(libs.retrofit.core)
    implementation(libs.kotlinx.serialization.json)
  }
}
