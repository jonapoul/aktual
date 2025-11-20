plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
  }
}
