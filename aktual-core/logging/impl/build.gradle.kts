import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
    api(libs.okio)
    api(project(":aktual-core:logging"))
    implementation(libs.kermit)
    implementation(libs.kotlinx.datetime)
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
