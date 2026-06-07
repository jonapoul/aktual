import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin { commonMainDependencies { api(project(":aktual-budget:data:db")) } }
