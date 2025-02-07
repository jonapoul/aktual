import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.kotlinx.datetime)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
