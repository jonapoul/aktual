plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
    api(project(":aktual-core:model"))
    compileOnly(libs.androidx.compose.annotation)
  }
}
