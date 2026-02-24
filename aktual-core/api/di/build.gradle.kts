import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:api:impl"))
  }
}
