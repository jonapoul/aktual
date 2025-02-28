import actual.gradle.androidMainDependencies
import actual.gradle.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

commonMainDependencies {
  api(libs.test.alakazam.core)
  api(libs.test.androidx.coreKtx)
  api(libs.test.kotlinx.coroutines)
  api(projects.db)
  implementation(libs.sqldelight.driver.sqlite)
}

androidMainDependencies {
  implementation(libs.sqldelight.driver.android)
}
