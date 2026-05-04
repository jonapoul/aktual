import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.navigation3.ui)
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.molecule)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    implementation(libs.compose.material3)
    implementation(libs.compose.savedstate)
    implementation(libs.compose.viewmodelNavigation3)
    implementation(libs.haze)
    implementation(libs.logcat)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:logging"))
  }
}
