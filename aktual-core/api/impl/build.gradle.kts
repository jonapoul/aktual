import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:prefs"))
    implementation(libs.ktor.auth)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test:api"))
  }
}
