plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(project(":aktual-updates"))
    implementation(libs.ktor.core)
    implementation(project(":aktual-di:core"))
    implementation(project(":aktual-logging"))
    implementation(project(":aktual-prefs"))
    implementation(project(":github-api"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
    implementation(project(":github-api:impl"))
  }
}
