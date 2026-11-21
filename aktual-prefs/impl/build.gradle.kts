plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefsCore)
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-prefs"))
    api(project(":aktual-theme:model"))
    implementation(libs.androidx.datastore.prefs)
    implementation(project(":aktual-di:core"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
