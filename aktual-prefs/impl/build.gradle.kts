import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies
import blueprint.core.jvmMainDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(project(":aktual-prefs"))
    api(project(":aktual-core:theme"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.prefs)
  }

  commonTestDependencies { implementation(project(":aktual-test")) }

  jvmMainDependencies { api(libs.compose.ui) }
}
