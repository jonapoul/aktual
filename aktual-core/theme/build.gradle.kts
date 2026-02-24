import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.jetbrains.uiGraphics)
    compileOnly(libs.jetbrains.runtime)
  }
}
