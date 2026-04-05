import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(libs.compose.navigation3)
    api(libs.compose.runtime)
    implementation(project(":aktual-core:logging"))
    implementation(libs.kotlinx.serialization.core)
  }
}
