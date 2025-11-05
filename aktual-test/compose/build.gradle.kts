plugins {
  alias(libs.plugins.module.compose)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:ui"))
    api(libs.jetbrains.resources)
    api(libs.jetbrains.runtime)
    api(libs.jetbrains.ui)
    api(libs.jetbrains.uiTest)
    api(libs.test.androidx.compose.ui.junit4)
    api(libs.test.kotlinx.coroutines)
  }

  jvmMainDependencies {
    api(compose.desktop.currentOs)
  }
}
