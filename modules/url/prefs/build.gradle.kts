import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.preferences.core)
    api(projects.url.model)
  }

  androidUnitTestDependencies {
    implementation(projects.test.buildconfig)
    implementation(projects.test.prefs)
  }
}
