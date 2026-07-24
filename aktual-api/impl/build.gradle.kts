import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.auth)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-api"))
    api(project(":aktual-budget"))
    api(project(":aktual-budget:data:proto"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-di:core"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.ktor.auth)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test:api"))
  }
}
