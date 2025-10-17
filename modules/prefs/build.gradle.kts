plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.preferences.core)
    api(project(":modules:core:model"))
  }

  androidMainDependencies {
    implementation(libs.preferences.android)
  }
}
