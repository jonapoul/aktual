plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(project(":aktual-core:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-codegen:annotation"))
  }
}

kspAllConfigs(project(":aktual-codegen:ksp"))
