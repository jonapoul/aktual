import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(project(":aktual-api:model"))
  }
}
