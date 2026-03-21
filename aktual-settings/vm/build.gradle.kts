import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:prefs"))
    api(project(":aktual-core:theme"))
  }

  commonTestDependencies {
    implementation(project(":aktual-core:prefs:di"))
    implementation(project(":aktual-test"))
  }
}
