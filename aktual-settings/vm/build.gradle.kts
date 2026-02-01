import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-prefs"))
    implementation(libs.preferences.core)
  }
}
