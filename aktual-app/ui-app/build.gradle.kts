plugins {
  id("aktual.module.di")
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    // UI modules - needed so Metro discovers @ContributesIntoSet nav entry contributors
    api(project(":aktual-about:ui"))
    api(project(":aktual-account:ui"))
    api(project(":aktual-budget:list:ui"))
    api(project(":aktual-budget:sync:components"))
    api(project(":aktual-metrics:ui"))
    api(project(":aktual-prefs:ui"))
  }
}
