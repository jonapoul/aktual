import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.jvmMainDependencies

plugins {
  alias(libs.plugins.module.composeMp)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui.text)
    api(libs.test.androidx.compose.ui.core)
    api(libs.test.kotlinx.coroutines)
    implementation(libs.androidx.compose.ui.core)
  }

  jvmMainDependencies {
    api(libs.test.androidx.compose.ui.junit4)
  }

  androidMainDependencies {
    api(project(":modules:core:ui")) // TODO: move to commonMain
    api(libs.test.androidx.compose.ui.junit4)
  }
}
