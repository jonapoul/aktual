import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-api:impl"))
  }
}
