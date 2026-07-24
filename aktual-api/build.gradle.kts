import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-api:model"))
    api(project(":aktual-core:theme:model"))
  }
}
