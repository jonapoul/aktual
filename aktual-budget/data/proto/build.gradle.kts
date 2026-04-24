import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:model"))
  }
}
