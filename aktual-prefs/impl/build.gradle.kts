import aktual.gradle.dsl.desktopMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-prefs"))
    api(project(":aktual-core:theme"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.prefs)
  }

  commonTestDependencies { implementation(project(":aktual-test")) }

  desktopMainDependencies { api(libs.compose.ui) }
}
