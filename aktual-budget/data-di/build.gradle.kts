import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.di")
}

kotlin {
  commonMainDependencies {
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:data-impl"))
  }
}
