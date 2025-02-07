import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
  api(libs.kotlinx.coroutines)
}

commonTestDependencies {
  implementation(projects.test.coroutines)
}
