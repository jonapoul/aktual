import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-account:domain"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    implementation(libs.androidx.datastore.core)
    implementation(libs.ktor.core)
    implementation(project(":aktual-api"))
  }

  commonTestDependencies {
    implementation(project(":aktual-api:impl"))
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-test:api"))
  }
}
