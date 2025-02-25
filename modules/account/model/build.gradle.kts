import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

commonMainDependencies {
  implementation(libs.alakazam.kotlin.serialization)
  implementation(libs.kotlinx.serialization.core)
}
