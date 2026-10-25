plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    api(project(":aktual-core:l10n"))
    implementation(libs.kotlinx.serialization.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
