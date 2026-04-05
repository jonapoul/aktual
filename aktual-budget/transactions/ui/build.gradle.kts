import aktual.gradle.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:transactions:vm"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-app:nav"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-core:shimmer"))
    implementation(libs.androidx.paging.compose)
    implementation(libs.kotlinx.datetime)
  }
}
