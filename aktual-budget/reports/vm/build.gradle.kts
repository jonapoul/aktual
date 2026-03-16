import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
  }

  androidHostTestDependencies { implementation(project(":aktual-app:di")) }
}
