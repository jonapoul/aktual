import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(project(":aktual-budget:sync:vm"))
    api(project(":aktual-core:ui"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-core:l10n"))
  }
}
