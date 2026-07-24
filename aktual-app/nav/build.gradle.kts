import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.navigation3.ui)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.immutable)
    api(libs.molecule)
    api(project(":aktual-core"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    implementation(libs.compose.viewmodelNavigation3)
    implementation(libs.logcat)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:logging"))
  }
}
