import blueprint.core.androidUnitTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.javaxInject)
    api(libs.preferences.core)
    api(projects.core.model)
  }

  androidUnitTestDependencies {
    implementation(projects.test.coroutines)
    implementation(projects.test.prefs)
  }
}
