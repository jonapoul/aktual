import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies { api(project(":aktual-core:api")) }

  commonTestDependencies {

    implementation(project(":aktual-test"))
  }
}
