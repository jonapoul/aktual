import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines)
    api(libs.test.alakazam.core)
    api(libs.test.kotlin.common)
    api(libs.test.turbine)
  }
}
