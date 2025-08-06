import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:list:vm"))
    api(project(":modules:core:ui"))
    api(compose.foundation)
    api(compose.runtime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:budget:model"))
    implementation(project(":modules:l10n"))
    implementation(compose.animation)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.preview)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.immutable)
  }

  androidMainDependencies {
    implementation(libs.androidx.coreKtx)
    implementation(libs.lazycolumn.scrollbar)
  }
}
