plugins {
  id("aktual.module.di")
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    // UI modules - needed so Metro discovers @ContributesIntoSet nav entry contributors
    api(project(":aktual-budget:accounts:ui"))
    api(project(":aktual-budget:banksync:ui"))
    api(project(":aktual-budget:budgeting:ui"))
    api(project(":aktual-budget:home:ui"))
    api(project(":aktual-budget:navrail:ui"))
    api(project(":aktual-budget:payees:ui"))
    api(project(":aktual-budget:reports:ui"))
    api(project(":aktual-budget:rules:ui"))
    api(project(":aktual-budget:schedules:ui"))
    api(project(":aktual-budget:tags:ui"))
    api(project(":aktual-budget:transactions:ui"))
  }
}
