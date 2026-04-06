import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:logging"))
    api(project(":aktual-core:model"))
    api(libs.compose.navigation3)
    api(libs.compose.runtime)
    implementation(libs.compose.savedstate)
    implementation(libs.logcat)
  }
}
