import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    // UI modules - needed so Metro discovers @ContributesIntoSet nav entry contributors
    api(project(":aktual-budget:navrail:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:rules:ui"))
    api(project(":aktual-budget:schedules:ui"))
    api(project(":aktual-budget:tags:ui"))
    api(project(":aktual-budget:transactions:ui"))
  }
}
