import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:api"))
    api(project(":aktual-core:model"))
    implementation(project(":aktual-budget:data"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-prefs"))
  }
}
