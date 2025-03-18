import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.kspAllConfigs

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.sekret)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.javaxInject)
    api(libs.kotlinx.serialization.json)
    api(libs.retrofit.core)
    api(projects.account.model)
    api(projects.budget.model)
    api(projects.url.model)
    compileOnly(libs.sekret)
    implementation(projects.codegen.annotation)
  }

  commonTestDependencies {
    implementation(projects.test.coroutines)
    implementation(projects.test.http)
  }
}

kspAllConfigs(projects.codegen.ksp)
