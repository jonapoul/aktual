plugins { id("aktual.module.di") }

kotlin {
  commonMainDependencies {
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-budget"))
    api(project(":aktual-core:utils"))
    implementation(libs.ktor.engineDefaults)
  }
}
