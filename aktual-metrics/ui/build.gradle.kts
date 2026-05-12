import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    api(project(":aktual-metrics:vm"))
    implementation(project(":aktual-core:l10n"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.shimmer)
  }
}
