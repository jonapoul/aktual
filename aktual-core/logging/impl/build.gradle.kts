import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:logging"))
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
  }
}
