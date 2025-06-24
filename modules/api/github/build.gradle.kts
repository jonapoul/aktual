import blueprint.core.commonMainDependencies
import blueprint.core.kspAllConfigs

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(projects.modules.core.model)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(projects.modules.codegen.annotation)
  }
}

kspAllConfigs(projects.modules.codegen.ksp)
