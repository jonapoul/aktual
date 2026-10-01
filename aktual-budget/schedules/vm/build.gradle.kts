import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    api(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
  }
}
