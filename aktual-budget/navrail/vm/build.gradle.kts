import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:nav"))
    api(project(":aktual-prefs"))
  }
}
