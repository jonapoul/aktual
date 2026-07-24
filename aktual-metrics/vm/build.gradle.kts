import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-api"))
    api(project(":aktual-di:core"))
    implementation(libs.kotlinx.serialization.core)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
