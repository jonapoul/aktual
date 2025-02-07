import actual.gradle.androidUnitTestDependencies
import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
  api(projects.url.model)
}

androidUnitTestDependencies {
  implementation(projects.test.buildconfig)
  implementation(projects.test.prefs)
}
