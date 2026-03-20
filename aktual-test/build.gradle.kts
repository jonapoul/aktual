import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(kotlin("test"))
    api(libs.alakazam.kotlin)
    api(libs.alakazam.test)
    api(libs.assertk)
    api(libs.burst)
    api(libs.burstCoroutines)
    api(libs.junit)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.logcat)
    api(libs.okio)
    api(libs.preferences.core)
    api(libs.turbine)
    api(project(":aktual-budget:data"))
    implementation(libs.sqldelight.driver.sqlite)
  }

  androidMainDependencies {
    api(libs.preferences.core)
    api(libs.androidx.test.coreKtx)
    api(project(":aktual-core:prefs:impl"))
    implementation(libs.preferences.android)
    implementation(libs.sqldelight.driver.android)
  }
}
