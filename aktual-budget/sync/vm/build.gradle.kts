import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:di"))
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-core:prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }
}
