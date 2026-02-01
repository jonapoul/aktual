import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.preferences.core)
    api(project(":aktual-budget:data"))
    api(project(":aktual-core:model"))
  }

  androidMainDependencies {
    implementation(libs.preferences.android)
  }
}
