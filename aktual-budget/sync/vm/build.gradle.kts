plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:encryption"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:di"))
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.time)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-api:actual"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }
}
