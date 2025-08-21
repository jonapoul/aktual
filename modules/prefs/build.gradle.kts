plugins {
  alias(libs.plugins.module.multiplatform)
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
