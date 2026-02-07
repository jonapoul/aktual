import blueprint.core.androidMainDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.di")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:prefs"))
    api(project(":aktual-core:prefs-impl"))
  }

  androidMainDependencies {
    implementation(libs.androidx.crypto)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.preferences.android)
  }
}
