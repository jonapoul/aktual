import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    api(libs.logcat)
    api(libs.test.alakazam)
    api(libs.test.assertk)
    api(libs.test.burst)
    api(libs.test.burstCoroutines)
    api(libs.test.junit)
    api(libs.test.kotlin.common)
    api(libs.test.kotlinx.coroutines)
    api(libs.test.ktor)
    api(libs.test.turbine)
    api(project(":aktual-budget:data"))
    implementation(libs.sqldelight.driver.sqlite)
    implementation(project(":aktual-core:api"))
  }

  androidMainDependencies {
    api(libs.test.androidx.coreKtx)
    implementation(libs.sqldelight.driver.android)
  }
}
