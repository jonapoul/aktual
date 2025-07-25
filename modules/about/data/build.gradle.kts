plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:api:github"))
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.javaxInject)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.alakazam.kotlin.compose.annotations)
  }
}
