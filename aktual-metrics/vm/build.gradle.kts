plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-api"))
    implementation(libs.kotlinx.serialization.core)
  }
}
