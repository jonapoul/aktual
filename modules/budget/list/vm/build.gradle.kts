plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
    api(libs.alakazam.kotlin.core)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:prefs"))
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization.core)
    implementation(libs.okio)
    implementation(libs.preferences.core)
  }
}
