import aktual.gradle.generateApiResponses
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:prefs"))
    implementation(libs.ktor.auth)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:model"))
  }
}

buildConfig {
  packageName("aktual.test")
  generateApiResponses(project, directory = "api/actual")
}
