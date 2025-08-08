plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:di"))
    api(project(":modules:budget:model"))
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.time)
    api(libs.kotlinx.datetime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:budget:encryption"))
    implementation(project(":modules:core:model"))
    implementation(project(":modules:prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }
}
