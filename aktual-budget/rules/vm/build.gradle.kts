plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    api(project(":aktual-core:l10n"))
    implementation(project(":aktual-budget:rules:domain"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
