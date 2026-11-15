plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:budgeting"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
    implementation(libs.kotlinx.immutable)
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-test"))
  }
}
