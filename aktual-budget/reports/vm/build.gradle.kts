plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:formula"))
    api(project(":aktual-budget:tags:domain"))
    implementation(project(":aktual-budget:budgeting:domain"))
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-core:theme"))
    implementation(project(":aktual-prefs"))
  }
}
