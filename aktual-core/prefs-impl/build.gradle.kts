import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.preferences.core)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:prefs"))
  }

  androidMainDependencies { implementation(libs.preferences.android) }
}
