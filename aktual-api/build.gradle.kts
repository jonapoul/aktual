plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-core:theme:model"))
  }
}
