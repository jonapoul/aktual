import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-di:runlevel"))
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }

  commonTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-test:api"))
  }
}
