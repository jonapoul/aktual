import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.multiplatform") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    implementation(libs.preferences.core)
    implementation(project(":aktual-core:api"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:prefs"))
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies { implementation(project(":aktual-test:api")) }

  androidHostTestDependencies { implementation(project(":aktual-core:api:impl")) }
}
