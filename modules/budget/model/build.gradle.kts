import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.serialization)
    api(libs.javaxInject)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
  }

  commonTestDependencies {
    implementation(projects.modules.test.files)
    implementation(projects.modules.test.json)
  }
}
