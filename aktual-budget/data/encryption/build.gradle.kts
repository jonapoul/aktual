plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
  }
}
