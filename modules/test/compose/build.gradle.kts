@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
  alias(libs.plugins.module.compose)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:core:ui"))
    api(compose.runtime)
    api(compose.ui)
    api(compose.uiTest)
    api(libs.test.kotlinx.coroutines)
  }

  jvmMainDependencies {
    api(compose.desktop.currentOs)
    api(compose.desktop.uiTestJUnit4)
  }
}
