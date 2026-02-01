import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-api:actual"))
    api(project(":aktual-api:builder"))
    api(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.preferences.core)
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-logging"))
    implementation(project(":aktual-prefs"))
  }
}
