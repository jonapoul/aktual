@file:Suppress("UnstableApiUsage")

plugins {
  id("aktual.module.kmp-android")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(libs.okio)
    api(project(":aktual-api"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-prefs"))
    compileOnly(libs.compose.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-di:core"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:bindings"))
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
