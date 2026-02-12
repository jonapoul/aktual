import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:reports:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:model"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.markdown.core)
    implementation(libs.markdown.m3)
    implementation(libs.vico.multiplatform)
  }
}
