import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.di")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-about:data"))
    api(project(":aktual-core:model"))
    implementation(project(":aktual-core:api"))
  }
}
