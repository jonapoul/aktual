plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(libs.kotlinx.datetime)
    implementation(project(":modules:core:di"))
    implementation(project(":modules:core:model"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.coroutines)
  }

  androidUnitTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":app:di"))
  }
}
