import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(project(":aktual-api"))
    api(project(":aktual-api:model"))
    api(project(":aktual-core"))
    api(project(":aktual-core:model"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-api:impl"))
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
