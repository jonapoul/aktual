plugins {
  id("aktual.module.viewmodel")
  alias(libs.plugins.fallbackSerializer)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    implementation(project(":aktual-budget:formula:domain"))
    implementation(project(":aktual-budget:rules:domain"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
