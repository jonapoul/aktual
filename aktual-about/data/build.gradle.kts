import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:model"))
  }
}
