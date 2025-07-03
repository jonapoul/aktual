import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    api(libs.test.alakazam.core)
    api(libs.test.junit)
    api(libs.test.kotlin.common)
    api(libs.test.kotlinx.coroutines)
    api(libs.test.ktor)
    api(libs.test.turbine)
    api(project(":modules:budget:data"))
    implementation(libs.alakazam.db.sqldelight)
    implementation(libs.sqldelight.driver.sqlite)
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:api:builder"))
  }

  androidMainDependencies {
    api(libs.test.androidx.coreKtx)
    implementation(libs.sqldelight.driver.android)
  }
}
