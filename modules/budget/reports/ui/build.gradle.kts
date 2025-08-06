import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:reports:vm"))
    api(project(":modules:core:ui"))
    api(compose.foundation)
    api(compose.runtime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:l10n"))
    implementation(compose.animation)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.preview)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.uiUtil)
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.immutable)
    implementation(libs.vico.multiplatform)
  }

  androidMainDependencies {
    implementation(libs.lazycolumn.scrollbar)
    implementation(libs.markdown.twain)
  }
}
