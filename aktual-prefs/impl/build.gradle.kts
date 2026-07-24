import aktual.gradle.dsl.desktopMainDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefsCore)
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-core:theme:model"))
    api(project(":aktual-prefs"))
    implementation(libs.androidx.datastore.prefs)
  }

  commonTestDependencies { implementation(project(":aktual-test")) }

  desktopMainDependencies { api(libs.compose.ui) }
}
