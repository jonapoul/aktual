import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
  }
}
