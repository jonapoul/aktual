import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    // UI modules - needed so Metro discovers @ContributesIntoSet nav entry contributors
    api(project(":aktual-about:ui"))
    api(project(":aktual-account:ui"))
    api(project(":aktual-budget:list:ui"))
    api(project(":aktual-budget:navrail:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:rules:ui"))
    api(project(":aktual-budget:schedules:ui"))
    api(project(":aktual-budget:sync:ui"))
    api(project(":aktual-budget:transactions:ui"))
    api(project(":aktual-metrics:ui"))
    api(project(":aktual-prefs:ui"))
  }
}
