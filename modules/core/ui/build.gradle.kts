import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.koverExcludes
import aktual.gradle.optIn

plugins {
  alias(libs.plugins.module.compose)
}

optIn(EXPERIMENTAL_MATERIAL_3)

koverExcludes {
  packages(
    "aktual.core.icons",
    "aktual.core.ui",
  )
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
    api(libs.haze.core)
    api(libs.kotlinx.datetime)
    implementation(project(":modules:core:di"))
    implementation(project(":modules:l10n"))
    implementation(libs.kotlinx.coroutines.core)
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.coreKtx)
  }
}
