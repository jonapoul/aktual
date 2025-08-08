plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.preferences.core)
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:api:builder"))
    implementation(project(":modules:core:model"))
    implementation(project(":modules:logging"))
    implementation(project(":modules:prefs"))
  }
}
