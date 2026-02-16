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
  }
}
