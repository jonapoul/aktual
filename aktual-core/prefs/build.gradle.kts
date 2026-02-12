import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(libs.preferences.core)
    api(project(":aktual-core:model"))
  }
}
