import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(kotlin("test"))
    api(libs.alakazam.kotlin)
    api(libs.alakazam.test)
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefs)
    api(libs.assertk)
    api(libs.burst)
    api(libs.burstCoroutines)
    api(libs.junit)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.test)
    api(libs.logcat)
    api(libs.okio)
    api(libs.turbine)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:data:prefs"))
    implementation(libs.sqldelight.driver.sqlite)
  }

  androidMainDependencies {
    api(libs.androidx.test.coreKtx)
    api(project(":aktual-prefs:impl"))
    implementation(libs.sqldelight.driver.android)
  }
}
