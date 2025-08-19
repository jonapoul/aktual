plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:encryption"))
    api(project(":modules:budget:model"))
    api(project(":modules:core:di"))
    api(libs.alakazam.kotlin.core)
    api(libs.alakazam.kotlin.time)
    api(libs.kotlinx.datetime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:core:model"))
    implementation(project(":modules:prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }
}
