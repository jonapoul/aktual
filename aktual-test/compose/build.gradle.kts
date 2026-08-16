import aktual.gradle.dsl.desktopMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.androidx.test.composeJunit4)
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.uiTest)
    api(libs.kotlinx.coroutines.test)
    api(project(":aktual-core:ui"))
  }

  desktopMainDependencies { api(compose.desktop.currentOs) }
}
