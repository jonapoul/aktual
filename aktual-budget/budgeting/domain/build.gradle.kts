plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    implementation(libs.kotlinx.immutable)
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-test"))
  }
}
