import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(libs.compose.animation)
    api(libs.compose.foundation)
    api(project(":aktual-budget:navrail:vm"))
    api(project(":aktual-di:runlevel"))
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.compose.navigation3.ui)
    implementation(libs.compose.runtime)
    implementation(libs.compose.viewmodelNavigation3)
    implementation(libs.haze)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.reorderable)
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:nav"))
    implementation(project(":aktual-core:ui"))
  }
}
