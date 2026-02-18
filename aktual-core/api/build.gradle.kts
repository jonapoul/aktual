@file:Suppress("UnstableApiUsage")

import aktual.gradle.generateApiResponses
import aktual.gradle.kspAllConfigs
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
  alias(libs.plugins.ksp)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(project(":aktual-budget:data"))
    api(project(":aktual-core:model"))
    implementation(libs.ktor.auth)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.preferences.core)
    implementation(project(":aktual-codegen:annotation"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:prefs"))
  }
}

kspAllConfigs(project(":aktual-codegen:ksp"))

buildConfig {
  packageName("aktual.test")
  generateApiResponses(project, directory = "api/actual")
}
