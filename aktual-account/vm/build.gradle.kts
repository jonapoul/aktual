import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-account:domain"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    api(libs.alakazam.kotlin)
    implementation(project(":aktual-core:api"))
    implementation(libs.ktor.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-core:api:impl"))
    implementation(project(":aktual-test:api"))
  }
}
