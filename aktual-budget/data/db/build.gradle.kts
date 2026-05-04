import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.sqldelight)
}

sqldelight {
  databases {
    register("BudgetDatabase") {
      packageName = "aktual.budget.db"
      schemaOutputDirectory = file("src/commonMain/sqldelight/schemas")
      verifyMigrations = true
      verifyDefinitions = true
      generateAsync = true
      dialect(libs.sqldelight.dialect)
      module(libs.sqldelight.json)
    }
  }
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.androidx.sqlite)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.androidx)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies { implementation(project(":aktual-test")) }
}
