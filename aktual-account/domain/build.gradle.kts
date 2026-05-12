import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:api"))
    api(project(":aktual-di:runlevel"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-core:logging"))
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies { implementation(project(":aktual-test:api")) }

  androidHostTestDependencies { implementation(project(":aktual-core:api:impl")) }
}
