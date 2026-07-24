import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(project(":aktual-about:vm"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-di:core"))
    implementation(libs.androidx.compose.annotation)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:theme:model"))
  }
}
