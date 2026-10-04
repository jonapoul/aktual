plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-account:domain"))
    api(project(":aktual-budget:demo"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.ktor.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-api:impl"))
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-test:api"))
  }
}
