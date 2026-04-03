import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:di"))
    api(project(":aktual-budget:model"))
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-prefs"))
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.okio)
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:data-di"))
    implementation(project(":aktual-core:api:di"))
    implementation(project(":aktual-test:api"))
  }
}
