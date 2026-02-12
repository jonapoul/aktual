import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    api(libs.alakazam.kotlin)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:prefs"))
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization.core)
    implementation(libs.okio)
    implementation(libs.preferences.core)
  }
}
