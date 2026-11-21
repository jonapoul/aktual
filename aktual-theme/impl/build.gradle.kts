@file:Suppress("UnstableApiUsage")

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(libs.okio)
    api(project(":aktual-api"))
    api(project(":aktual-prefs"))
    api(project(":aktual-theme"))
    compileOnly(libs.compose.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-di:core"))
    implementation(project(":aktual-logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:bindings"))
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
