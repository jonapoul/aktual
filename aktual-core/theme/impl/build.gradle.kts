@file:Suppress("UnstableApiUsage")

import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(libs.okio)
    api(project(":aktual-api"))
    api(project(":aktual-core"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-core:theme:model"))
    api(project(":aktual-di:core"))
    api(project(":aktual-prefs"))
    compileOnly(libs.compose.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:bindings"))
    implementation(project(":aktual-test:api"))
  }
}
