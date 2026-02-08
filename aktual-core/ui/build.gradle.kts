import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
}

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:icons"))
    api(project(":aktual-core:model"))
    api(libs.haze.core)
    api(libs.kotlinx.datetime)
    api(libs.shimmer)
    implementation(project(":aktual-core:l10n"))
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.coreKtx)
  }
}
