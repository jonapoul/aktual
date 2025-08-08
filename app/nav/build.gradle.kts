plugins {
  alias(libs.plugins.module.compose)
}

kotlin {
  commonMainDependencies {
    api(compose.components.resources)
    api(compose.runtime)
    implementation(project(":modules:about:ui"))
    implementation(project(":modules:account:ui"))
    implementation(project(":modules:budget:list:ui"))
    implementation(project(":modules:budget:reports:ui"))
    implementation(project(":modules:budget:sync:ui"))
    implementation(project(":modules:budget:transactions:ui"))
    implementation(project(":modules:core:ui"))
    implementation(project(":modules:logging"))
    implementation(project(":modules:settings:ui"))
    implementation(compose.material3)
    implementation(libs.jetbrains.navigation)
    implementation(libs.logcat)
  }
}
