import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.viewmodel")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:domain"))
    api(project(":aktual-core:model"))
    api(project(":aktual-core:prefs"))
    api(libs.alakazam.kotlin)
    implementation(project(":aktual-core:api"))
    implementation(libs.ktor.core)
    implementation(libs.preferences.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-core:api"))
  }
}
