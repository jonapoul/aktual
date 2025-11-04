@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
  alias(libs.plugins.module.compose)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:ui"))
    api(compose.components.resources)
    api(compose.runtime)
    api(compose.ui)
    api(compose.uiTest)
    api(libs.test.androidx.compose.ui.junit4)
    api(libs.test.kotlinx.coroutines)
  }

  jvmMainDependencies {
    api(compose.desktop.currentOs)
  }
}
