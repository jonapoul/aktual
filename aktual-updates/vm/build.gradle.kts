plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    implementation(libs.kotlinx.datetime)
    implementation(project(":aktual-updates:domain"))
  }
}
