import blueprint.core.commonMainDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    implementation(libs.preferences.core)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:prefs"))
    compileOnly(libs.androidx.compose.annotation)
  }

  androidHostTestDependencies { implementation(project(":aktual-core:api")) }
}
