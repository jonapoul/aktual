import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(compose.runtime)
    api(libs.androidx.lifecycle.viewmodel.core)
    api(project(":modules:about:vm"))
    api(project(":modules:core:ui"))
    implementation(compose.animation)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.preview)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.immutable)
    implementation(project(":modules:l10n"))
  }

  androidMainDependencies {
    implementation(project(":modules:core:android"))
    implementation(libs.lazycolumn.scrollbar)
  }
}
