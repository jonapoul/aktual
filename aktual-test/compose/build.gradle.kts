import blueprint.core.commonMainDependencies
import blueprint.core.jvmMainDependencies

plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:ui"))
    api(libs.compose.resources)
    api(libs.compose.runtime)
    api(libs.compose.ui)
    api(libs.compose.uiTest)
    api(libs.test.androidx.compose.ui.junit4)
    api(libs.test.kotlinx.coroutines)
  }

  jvmMainDependencies { api(compose.desktop.currentOs) }
}
