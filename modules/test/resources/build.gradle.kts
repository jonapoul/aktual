import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  implementation(libs.test.alakazam.core)
}
