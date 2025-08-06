plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:prefs"))
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.immutable)
    implementation(compose.runtime)
    implementation(libs.preferences.core)
  }
}
