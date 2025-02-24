import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.okhttp.core)
  api(projects.budget.model)
  implementation(projects.api.core)
}

commonTestDependencies {
  implementation(projects.api.builder)
  implementation(projects.test.coroutines)
  implementation(projects.test.http)
}
