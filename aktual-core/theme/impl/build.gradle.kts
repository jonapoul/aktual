@file:Suppress("UnstableApiUsage")

import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.jetbrains.uiGraphics)
    api(project(":aktual-core:api"))
    api(project(":aktual-core:theme"))
    compileOnly(libs.jetbrains.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:data-di"))
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:prefs:di"))
    implementation(project(":aktual-core:theme:di"))
    implementation(project(":aktual-test:api"))
  }
}
