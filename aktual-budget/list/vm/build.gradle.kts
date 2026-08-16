import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-di:runlevel"))
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization.core)
    implementation(libs.okio)
    implementation(project(":aktual-api"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-api:impl"))
    implementation(project(":aktual-test:api"))
  }
}
