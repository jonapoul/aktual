import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
