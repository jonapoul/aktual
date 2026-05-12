import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.metrox.viewmodel)
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(project(":aktual-di:core"))
  }
}
