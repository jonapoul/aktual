plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:account:domain"))
    api(project(":modules:core:model"))
    api(project(":modules:prefs"))
    api(libs.alakazam.kotlin.core)
    implementation(project(":modules:api:actual"))
    implementation(libs.ktor.core)
    implementation(libs.preferences.core)
  }

  commonTestDependencies {
    implementation(project(":modules:core:connection"))
  }
}
