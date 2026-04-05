import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-app:nav"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(project(":aktual-prefs"))
  }
}
