plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:api:actual"))
    api(project(":modules:api:builder"))
    api(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.preferences.core)
    implementation(project(":modules:core:model"))
    implementation(project(":modules:logging"))
    implementation(project(":modules:prefs"))
  }
}
