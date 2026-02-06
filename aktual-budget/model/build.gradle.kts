import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    compileOnly(libs.androidx.compose.annotation)
  }
}
