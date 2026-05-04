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
    api(project(":aktual-core:api"))
    api(project(":aktual-core:theme"))
    api(project(":aktual-prefs"))
    compileOnly(libs.compose.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:bindings"))
    implementation(project(":aktual-test:api"))
  }
}
