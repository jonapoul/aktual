import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(projects.modules.api.github)
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.javaxInject)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }

  commonTestDependencies {
    implementation(projects.modules.test.buildconfig)
    implementation(projects.modules.test.coroutines)
    implementation(projects.modules.test.http)
  }
}
