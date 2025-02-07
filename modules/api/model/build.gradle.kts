import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.sekret)
}

commonMainDependencies {
  api(libs.alakazam.kotlin.serialization)
  api(libs.kotlinx.serialization.core)
  api(projects.login.model)
  implementation(libs.kotlinx.serialization.json)
  compileOnly(libs.sekret)
}

commonTestDependencies {
  implementation(projects.api.json)
}
