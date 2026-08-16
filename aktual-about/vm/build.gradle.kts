import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefsCore)
    api(libs.kotlinx.serialization.core)
    api(project(":aktual-about:data"))
    api(project(":aktual-budget"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-di:core"))
    api(project(":aktual-di:runlevel"))
    implementation(libs.okio)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
