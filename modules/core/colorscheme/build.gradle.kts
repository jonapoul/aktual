import actual.gradle.androidUnitTestDependencies
import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.javaxInject)
  api(libs.preferences.core)
}

androidUnitTestDependencies {
  implementation(projects.test.coroutines)
  implementation(projects.test.prefs)
}
