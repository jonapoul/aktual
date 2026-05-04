import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:list:vm"))
    api(project(":aktual-core:nav"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-budget:sync:ui"))
    implementation(project(":aktual-core:l10n"))
  }

  androidMainDependencies { implementation(libs.androidx.core) }
}
