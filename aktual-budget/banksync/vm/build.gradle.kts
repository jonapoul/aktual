import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:banksync:domain"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(project(":aktual-api"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-test"))
  }
}
