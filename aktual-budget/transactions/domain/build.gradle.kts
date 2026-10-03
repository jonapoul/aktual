import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
