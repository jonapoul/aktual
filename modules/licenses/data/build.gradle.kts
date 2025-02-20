import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

commonMainDependencies {
  api(libs.kotlinx.serialization.core)
  implementation(libs.alakazam.kotlin.core)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.json)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}

commonTestDependencies {
  implementation(projects.test.coroutines)
}
