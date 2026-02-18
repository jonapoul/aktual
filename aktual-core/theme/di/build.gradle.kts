import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:theme"))
    api(project(":aktual-core:theme:impl"))
  }
}
