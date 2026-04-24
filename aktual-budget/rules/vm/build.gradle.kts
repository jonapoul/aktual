import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:di"))
    api(project(":aktual-budget:model"))
    implementation(project(":aktual-core:model"))
  }
}
