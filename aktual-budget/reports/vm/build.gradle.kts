import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:data:db"))
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-budget:di"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies { implementation(libs.sqldelight.coroutines) }

  androidHostTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":aktual-app:di"))
  }
}
