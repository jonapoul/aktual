import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.prefs)
    api(project(":aktual-prefs"))
    api(project(":aktual-prefs:impl"))
  }

  androidMainDependencies { implementation(libs.androidx.crypto) }
}
