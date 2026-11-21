plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:reports"))
    api(project(":aktual-budget:rules"))
    api(project(":aktual-budget:tags"))
    api(project(":aktual-budget:utils"))
    implementation(project(":aktual-budget:budgeting"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-prefs"))
    implementation(project(":aktual-theme"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:budgeting:impl"))
    implementation(project(":aktual-budget:rules:impl"))
  }
}
