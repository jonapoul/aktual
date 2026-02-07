import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
  alias(libs.plugins.wire)
}

wire {
  kotlin {}
}

kotlin {
  commonMainDependencies {
    api(libs.okio)
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:model"))
  }
}
