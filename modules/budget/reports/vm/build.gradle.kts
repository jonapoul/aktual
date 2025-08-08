plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:model"))
    api(libs.kotlinx.datetime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:budget:di"))
    implementation(project(":modules:core:model"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.coroutines)
  }

  androidUnitTestDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
