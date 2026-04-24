import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(project(":aktual-budget:data:encryption"))
    api(project(":aktual-core:model"))
  }

  commonTestDependencies { implementation(project(":aktual-test:api")) }
}
