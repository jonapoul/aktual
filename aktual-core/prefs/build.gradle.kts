import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(libs.preferences.core)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
  }
}
