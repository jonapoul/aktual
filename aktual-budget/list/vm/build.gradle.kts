plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(libs.alakazam.kotlin.core)
    implementation(project(":aktual-api:actual"))
    implementation(project(":aktual-prefs"))
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization.core)
    implementation(libs.okio)
    implementation(libs.preferences.core)
  }
}
