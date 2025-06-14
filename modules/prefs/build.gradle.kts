import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.preferences.core)
    api(projects.modules.account.model)
    api(projects.modules.core.model)
  }

  androidMainDependencies {
    implementation(libs.preferences.android)
  }
}
