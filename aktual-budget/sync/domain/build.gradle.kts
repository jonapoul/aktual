import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:api"))
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:data:proto"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-prefs"))
  }
}
