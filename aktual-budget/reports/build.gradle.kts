plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.coroutines.core)
    api(project(":aktual-budget:formula"))
    api(project(":aktual-budget:model"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(project(":aktual-logging"))
  }
}
