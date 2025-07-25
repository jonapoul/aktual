plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(project(":modules:core:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(project(":modules:codegen:annotation"))
  }
}

kspAllConfigs(project(":modules:codegen:ksp"))
