import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.compose")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.navigation3.runtime)
    api(libs.compose.runtime)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(project(":aktual-core:logging"))
  }
}
