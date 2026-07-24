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
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.json)
    api(libs.okio)
    api(project(":aktual-budget"))
    api(project(":aktual-di:core"))
    implementation(libs.androidx.sqliteBundled)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.androidx)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
