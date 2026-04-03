import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
  }
}
