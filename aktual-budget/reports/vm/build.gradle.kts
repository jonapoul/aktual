import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:data"))
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies { implementation(libs.sqldelight.coroutines) }

  androidHostTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":aktual-app:di"))
  }
}
