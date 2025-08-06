import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(compose.foundation)
    api(compose.runtime)
    api(project(":modules:budget:model"))
    api(project(":modules:budget:transactions:vm"))
    api(project(":modules:core:ui"))
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.preview)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.uiUtil)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.immutable)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:l10n"))
  }

  androidMainDependencies {
    implementation(libs.lazycolumn.scrollbar)
  }
}
