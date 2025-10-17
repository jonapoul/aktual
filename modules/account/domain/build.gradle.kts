plugins {
  alias(libs.plugins.module.multiplatform)
}

kotlin {
  commonMainDependencies {
    api(project(":modules:core:model"))
    implementation(libs.alakazam.kotlin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.preferences.core)
    implementation(project(":modules:api:actual"))
    implementation(project(":modules:logging"))
    implementation(project(":modules:prefs"))
    compileOnly(libs.androidx.compose.annotation)
  }

  androidUnitTestDependencies {
    implementation(project(":modules:api:builder"))
    implementation(project(":modules:core:connection"))
  }
}
