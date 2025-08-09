plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:prefs"))
    implementation(libs.preferences.core)
  }
}
