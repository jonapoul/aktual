import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.kspAllConfigs

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.sekret)
  alias(libs.plugins.ksp)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.javaxInject)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(libs.okio)
    api(projects.modules.account.model)
    api(projects.modules.budget.model)
    api(projects.modules.core.model)
    compileOnly(libs.sekret)
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(projects.modules.codegen.annotation)
  }

  commonTestDependencies {
    implementation(projects.modules.test.coroutines)
    implementation(projects.modules.test.http)
  }
}

kspAllConfigs(projects.modules.codegen.ksp)
