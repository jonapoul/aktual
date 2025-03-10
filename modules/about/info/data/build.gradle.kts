import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.retrofit.core)
    api(projects.api.github)
    api(libs.alakazam.kotlin.core)
    implementation(libs.javaxInject)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
  }

  commonTestDependencies {
    implementation(projects.test.buildconfig)
    implementation(projects.test.coroutines)
    implementation(projects.test.http)
  }
}
