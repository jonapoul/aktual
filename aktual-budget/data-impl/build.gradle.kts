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
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    implementation(libs.androidx.sqlite)
    implementation(libs.room.runtime)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
