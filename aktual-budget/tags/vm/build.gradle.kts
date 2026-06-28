import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:data:db"))
    implementation(project(":aktual-prefs"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-prefs:impl"))
    implementation(project(":aktual-test"))
  }
}
