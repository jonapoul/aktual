plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.lifecycle.viewmodel.core)
    api(project(":modules:core:di"))
  }
}
