import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
