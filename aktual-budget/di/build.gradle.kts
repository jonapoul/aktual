import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.di)
}

kotlin {
  commonMainDependencies {
    api(libs.sqldelight.runtime)
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:di"))
  }
}
