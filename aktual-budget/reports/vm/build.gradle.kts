plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:reports"))
    api(project(":aktual-budget:rules:impl"))
    api(project(":aktual-budget:tags:impl"))
    implementation(project(":aktual-budget:budgeting:impl"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:theme"))
    implementation(project(":aktual-prefs"))
  }
}
