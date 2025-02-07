import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.alakazam.kotlin.core)
  api(libs.javaxInject)
  api(libs.retrofit.core)
  api(projects.api.model)
  api(projects.url.model)
}
