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
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(project(":aktual-budget:data"))
    api(project(":aktual-core:model"))
    implementation(libs.preferences.core)
    implementation(project(":aktual-codegen:annotation"))
  }
}

kspAllConfigs(project(":aktual-codegen:ksp"))

buildConfig {
  packageName("aktual.test")
  generateApiResponses(project, directory = "api/actual")
}
