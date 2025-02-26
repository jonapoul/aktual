import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
}
