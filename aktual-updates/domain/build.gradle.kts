plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.core)
    implementation(project(":aktual-api"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-di:core"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-api:impl"))
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
