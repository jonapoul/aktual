import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.test.junit)
  api(libs.test.okhttp)
  api(projects.url.model)
  implementation(libs.okhttp.core)
}
