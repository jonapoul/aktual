import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.compose.material3WindowSize)
    api(libs.haze)
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:icons"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:shimmer"))
    api(project(":aktual-core:theme"))
    implementation(project(":aktual-core:l10n"))
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.coreKtx)
  }
}
