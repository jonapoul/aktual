import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.javaxInject)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.core)
    api(libs.okio)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }
}
