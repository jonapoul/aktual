import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.serialization)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
  }
}
