import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:data-di"))
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-prefs:di"))
    implementation(project(":aktual-test"))
  }
}
