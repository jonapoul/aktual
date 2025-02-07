import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(projects.core.buildconfig)
  compileOnly(libs.alakazam.kotlin.compose.annotations)
}
