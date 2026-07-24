import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    implementation(libs.kotlinx.serialization.core)
  }
}
