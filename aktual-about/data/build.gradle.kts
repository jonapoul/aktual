plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-api:github"))
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    compileOnly(libs.androidx.compose.annotation)
  }
}
