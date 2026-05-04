import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.metrox.viewmodel)
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:data:prefs"))
    api(project(":aktual-core:api"))
    implementation(project(":aktual-core:logging"))
  }
}
