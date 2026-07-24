import aktual.gradle.dsl.desktopMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core"))
  }

  desktopMainDependencies { api(libs.compose.ui) }
}
