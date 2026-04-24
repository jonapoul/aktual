import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:data:impl"))
  }
}
