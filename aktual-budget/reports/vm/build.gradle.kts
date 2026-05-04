import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-budget:data:db"))
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-budget:di"))
    implementation(project(":aktual-core:model"))
  }
}
