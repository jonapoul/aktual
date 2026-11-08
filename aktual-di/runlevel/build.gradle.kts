plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.metrox.viewmodel)
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget"))
    api(project(":aktual-core:model"))
  }
}
