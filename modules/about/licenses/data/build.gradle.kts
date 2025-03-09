import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.core)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.javaxInject)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.core.files)
  }

  commonTestDependencies {
    implementation(projects.test.coroutines)
  }
}
