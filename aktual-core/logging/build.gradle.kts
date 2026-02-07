import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
}

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
  }
}
