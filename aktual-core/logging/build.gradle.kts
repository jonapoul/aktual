import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.logcat)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
