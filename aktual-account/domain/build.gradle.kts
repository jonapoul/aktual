import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-api"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.ktor.core)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies { implementation(project(":aktual-test:api")) }

  androidHostTestDependencies { implementation(project(":aktual-api:impl")) }
}
