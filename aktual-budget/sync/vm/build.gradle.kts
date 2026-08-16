import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget"))
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-core"))
    api(project(":aktual-di:runlevel"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
    implementation(project(":aktual-api"))
    implementation(project(":aktual-prefs"))
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-test:api"))
  }
}
