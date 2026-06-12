import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  id("aktual.convention.db-test")
  alias(libs.plugins.sqldelight)
  idea
}

sqldelight {
  databases {
    register("BudgetDatabase") {
      packageName = "aktual.budget.db"
      schemaOutputDirectory = file("src/commonMain/sqldelight/schemas")
      verifyMigrations = false
      verifyDefinitions = true
      generateAsync = true
      dialect(libs.sqldelight.dialect)
      module(libs.sqldelight.json)
    }
  }
}

// Run SQLDelight interface generation on every IDE sync
tasks.named("prepareKotlinIdeaImport") {
  dependsOn("generateSqlDelightInterface")
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.androidx.sqliteBundled)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.androidx)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.driver.sqlite)
    implementation(project(":aktual-test"))
  }
}
