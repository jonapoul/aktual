plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-prefs"))
  }

  androidUnitTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":aktual-app:di"))
  }
}
