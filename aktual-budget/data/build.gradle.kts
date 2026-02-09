import blueprint.core.androidMainDependencies
import blueprint.core.boolProperty
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.multiplatform")
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

buildConfig {
  forClass("DatabaseBuildConfig") {
    packageName("aktual.budget.db")
    buildConfigField<Boolean>(
        "FOREIGN_KEY_CONSTRAINTS",
        providers.boolProperty(key = "aktual.db.foreignKeyConstraints"),
    )
  }
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-core:logging"))
  }

  androidMainDependencies { implementation(libs.sqldelight.driver.android) }
}
