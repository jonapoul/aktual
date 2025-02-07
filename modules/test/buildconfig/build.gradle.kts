import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(projects.core.buildconfig)
  api(libs.kotlinx.datetime)
}
