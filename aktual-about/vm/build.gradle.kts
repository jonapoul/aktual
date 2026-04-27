import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-about:data"))
    api(project(":aktual-budget:di"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.prefs)
    implementation(project(":aktual-core:theme"))
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
