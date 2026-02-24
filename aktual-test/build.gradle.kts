import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(kotlin("test"))
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.logcat)
    api(libs.okio)
    api(libs.test.alakazam)
    api(libs.test.assertk)
    api(libs.test.burst)
    api(libs.test.burstCoroutines)
    api(libs.test.junit)
    api(libs.test.kotlinx.coroutines)
    api(libs.test.turbine)
    api(project(":aktual-budget:data"))
    implementation(libs.sqldelight.driver.sqlite)
  }

  androidMainDependencies {
    api(libs.preferences.core)
    api(libs.test.androidx.coreKtx)
    api(project(":aktual-core:prefs:impl"))
    implementation(libs.preferences.android)
    implementation(libs.sqldelight.driver.android)
  }
}
