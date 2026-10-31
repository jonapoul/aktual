plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(project(":aktual-budget:data:db"))
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-core:l10n"))
  }
}
