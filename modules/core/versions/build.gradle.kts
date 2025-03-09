import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.javaxInject)
    api(projects.core.buildconfig)
    implementation(libs.kotlinx.coroutines)
  }
}
