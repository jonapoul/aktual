import blueprint.core.boolProperty

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.buildconfig)
}

sqldelight {
  databases {
    create("BudgetDatabase") {
      packageName = "aktual.budget.db"
      schemaOutputDirectory = file("src/commonMain/sqldelight/schemas")
      verifyMigrations = true
      verifyDefinitions = true
      generateAsync = true
      dialect(libs.sqldelight.dialect)
      module(libs.sqldelight.json.get())
    }
  }
}

buildConfig {
  packageName("aktual.budget.db")
  className("DatabaseBuildConfig")
  buildConfigField(name = "FOREIGN_KEY_CONSTRAINTS", value = boolProperty(key = "aktual.db.foreignKeyConstraints"))
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.preferences.core)
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
    implementation(libs.alakazam.db.sqldelight)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":modules:logging"))
  }

  commonTestDependencies {
    implementation(libs.test.alakazam.core)
    implementation(libs.test.alakazam.sqldelight)
  }

  androidMainDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
