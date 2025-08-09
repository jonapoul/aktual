plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":modules:account:model"))
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(project(":modules:codegen:annotation"))
    compileOnly(libs.redacted.annotations)
  }

  commonTestDependencies {
    implementation(project(":modules:test:api"))
  }
}

kspAllConfigs(project(":modules:codegen:ksp"))
