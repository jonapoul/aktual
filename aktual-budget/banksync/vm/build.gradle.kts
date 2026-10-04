import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    implementation(project(":aktual-api"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
