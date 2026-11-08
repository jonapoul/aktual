plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-budget"))
    api(project(":aktual-core:utils"))
    implementation(libs.ktor.engineDefaults)
  }
}
