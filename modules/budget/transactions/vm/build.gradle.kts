plugins {
  alias(libs.plugins.module.viewmodel)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:budget:data"))
    api(project(":modules:budget:model"))
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.datetime)
    implementation(project(":modules:account:model"))
    implementation(project(":modules:core:di"))
    implementation(project(":modules:prefs"))
  }

  androidUnitTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":app:di"))
  }
}
