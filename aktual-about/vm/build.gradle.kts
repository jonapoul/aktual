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
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-theme"))
    implementation(libs.okio)
  }
}
