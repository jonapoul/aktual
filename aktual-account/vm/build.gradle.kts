plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:domain"))
    api(project(":aktual-core:model"))
    api(project(":aktual-prefs"))
    api(libs.alakazam.kotlin.core)
    implementation(project(":aktual-api:actual"))
    implementation(libs.ktor.core)
    implementation(libs.preferences.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-core:connection"))
  }
}
