plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    implementation(libs.kotlinx.datetime)
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-updates:domain"))
  }
}
