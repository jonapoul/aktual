import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.serialization)
    api(libs.javaxInject)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
  }

  commonTestDependencies {
    implementation(projects.test.json)
  }
}
