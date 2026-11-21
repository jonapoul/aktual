plugins { id("aktual.module.compose") }

kotlin {
  commonMainDependencies {
    api(libs.compose.ui)
    api(libs.kotlinx.coroutines.core)
    api(project(":aktual-about:data"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:ui"))
    implementation(project(":aktual-logging"))
  }

  androidMainDependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
  }
}
