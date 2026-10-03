import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:model"))
    api(project(":aktual-core"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
