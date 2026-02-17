@file:Suppress("UnstableApiUsage")

import aktual.gradle.addResponsesClass
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
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
}

buildConfig {
  sourceSets.named("test") {
    packageName("aktual.test")
    addResponsesClass(project, rootProject.isolated.projectDirectory.file("api/theme").asFile)
  }
}
