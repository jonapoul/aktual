import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-prefs"))
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies { implementation(project(":aktual-test:api")) }

  androidHostTestDependencies { implementation(project(":aktual-core:api:impl")) }
}
