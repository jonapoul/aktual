import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(libs.preferences.core)
    compileOnly(libs.compose.runtime)
  }
}
