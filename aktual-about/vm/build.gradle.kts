import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-about:data"))
    api(libs.alakazam.kotlin)
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
