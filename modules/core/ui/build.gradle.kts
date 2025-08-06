import actual.gradle.EXPERIMENTAL_MATERIAL_3
import actual.gradle.koverExcludes
import actual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

koverExcludes {
  packages(
    "actual.core.icons",
    "actual.core.ui",
  )
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
    api(compose.runtime)
    api(libs.haze.core)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.immutable)
    implementation(project(":modules:di:core"))
    implementation(project(":modules:l10n"))
    implementation(compose.animation)
    implementation(compose.foundation)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.uiUtil)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines)
  }

  androidMainDependencies {
    api(libs.lazycolumn.scrollbar)
    implementation(project(":modules:core:android"))
    implementation(libs.androidx.coreKtx)
  }
}
