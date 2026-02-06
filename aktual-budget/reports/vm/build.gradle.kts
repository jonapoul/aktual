import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.coroutines)
  }

  androidHostTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":aktual-app:di"))
  }
}
