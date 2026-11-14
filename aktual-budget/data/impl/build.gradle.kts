plugins {
  id("aktual.module.kotlin")
  id("aktual.convention.db-test")
  alias(libs.plugins.wire)
}

wire { kotlin {} }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.okio)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-budget:data:proto"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
    implementation(libs.androidx.sqliteBundled)
    implementation(libs.sqldelight.driver.androidx)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
