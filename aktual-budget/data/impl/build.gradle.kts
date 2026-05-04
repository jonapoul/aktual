import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.wire)
}

wire { kotlin {} }

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-budget:data:proto"))
    api(project(":aktual-budget:data:prefs"))
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
