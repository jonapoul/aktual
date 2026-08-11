import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3, "dev.chrisbanes.haze.ExperimentalHazeApi")

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(libs.compose.material3)
    api(libs.compose.material3WindowSize)
    api(libs.compose.ui)
    api(libs.compose.uiToolingPreview)
    api(libs.haze.blur)
    api(libs.haze.glass)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.immutable)
    api(libs.shimmer)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core"))
    api(project(":aktual-core:icons"))
    api(project(":aktual-core:theme"))
    implementation(libs.compose.resources)
    implementation(project(":aktual-core:l10n"))
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
  }
}
