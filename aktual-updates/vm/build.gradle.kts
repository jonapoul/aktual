plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    implementation(libs.kotlinx.datetime)
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:utils"))
    implementation(project(":aktual-updates:domain"))
  }
}
