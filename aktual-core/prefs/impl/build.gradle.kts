import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

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

  commonTestDependencies { implementation(project(":aktual-test")) }

  androidMainDependencies { implementation(libs.preferences.android) }
}
