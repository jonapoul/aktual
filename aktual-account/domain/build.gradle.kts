plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.preferences.core)
    implementation(project(":aktual-api:actual"))
    implementation(project(":aktual-logging"))
    implementation(project(":aktual-prefs"))
    compileOnly(libs.androidx.compose.annotation)
  }

  androidUnitTestDependencies {
    implementation(project(":aktual-api:builder"))
    implementation(project(":aktual-core:connection"))
  }
}
