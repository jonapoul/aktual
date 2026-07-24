import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.metrox.viewmodel)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    implementation(libs.sqldelight.runtime)
  }
}
