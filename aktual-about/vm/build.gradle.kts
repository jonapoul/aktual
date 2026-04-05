import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-about:data"))
    api(project(":aktual-budget:di"))
    api(libs.alakazam.kotlin)
    implementation(project(":aktual-core:theme"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
