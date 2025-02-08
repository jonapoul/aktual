import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

commonMainDependencies {
  api(libs.retrofit.core)
  api(projects.api.github)
  api(projects.core.buildconfig)
  api(projects.core.coroutines)
  implementation(libs.javaxInject)
  implementation(libs.kotlinx.coroutines)
  implementation(libs.kotlinx.serialization.core)
  implementation(libs.kotlinx.serialization.json)
}

commonTestDependencies {
  implementation(projects.test.buildconfig)
  implementation(projects.test.coroutines)
  implementation(projects.test.http)
  implementation(projects.test.resources)
}
