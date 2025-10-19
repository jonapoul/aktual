import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:list:vm"))
    api(project(":modules:core:ui"))
    implementation(project(":modules:budget:model"))
    implementation(project(":modules:core:model"))
    implementation(project(":modules:l10n"))
    implementation(libs.kotlinx.coroutines.core)
  }

  androidMainDependencies {
    implementation(libs.androidx.coreKtx)
  }
}
