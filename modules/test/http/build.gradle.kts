import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp.core)
    api(libs.retrofit.core)
    api(libs.test.junit)
    api(libs.test.okhttp)
    api(projects.url.model)
    implementation(projects.api.builder)
  }
}
