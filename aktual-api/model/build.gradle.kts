import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.compose.runtime)
    api(libs.kotlinx.serialization.json)
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-core:theme:model"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test:api"))
  }
}
