import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-app:nav"))
    api(project(":aktual-budget:di"))
    api(project(":aktual-core:api"))
    api(project(":aktual-core:l10n"))
    api(project(":aktual-core:logging"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-prefs"))
    api(libs.compose.navigation3)
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.molecule)
    implementation(libs.compose.material3)
    implementation(libs.haze)
    implementation(libs.compose.savedstate)
    implementation(libs.compose.viewmodelNavigation3)
    implementation(libs.logcat)
    implementation(project(":aktual-app:nav:core"))
  }
}
