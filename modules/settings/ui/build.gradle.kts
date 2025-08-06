import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:core:model"))
    api(project(":modules:core:ui"))
    api(project(":modules:settings:vm"))
    api(compose.foundation)
    api(compose.runtime)
    implementation(project(":modules:l10n"))
    implementation(compose.animation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.preview)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.uiUtil)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.haze.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.immutable)
  }

  androidMainDependencies {
    implementation(libs.lazycolumn.scrollbar)
  }
}
