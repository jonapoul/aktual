plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.uiGraphics)
    api(project(":aktual-theme:model"))
    compileOnly(libs.compose.runtime)
    implementation(project(":aktual-core:logging"))
  }
}
