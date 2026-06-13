import aktual.gradle.dsl.desktopMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:ui"))
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.uiTest)
    api(libs.androidx.test.composeJunit4)
    api(libs.kotlinx.coroutines.test)
  }

  desktopMainDependencies { api(compose.desktop.currentOs) }
}
