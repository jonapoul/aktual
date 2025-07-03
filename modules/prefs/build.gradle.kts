import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.preferences.core)
    api(project(":modules:account:model"))
    api(project(":modules:core:model"))
  }

  androidMainDependencies {
    implementation(libs.preferences.android)
  }
}
