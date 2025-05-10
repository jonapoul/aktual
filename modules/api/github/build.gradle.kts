import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
  }
}
