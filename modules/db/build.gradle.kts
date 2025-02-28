import actual.gradle.androidMainDependencies
import actual.gradle.commonMainDependencies
import actual.gradle.commonTestDependencies
import blueprint.core.boolProperty

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.buildconfig)
}

sqldelight {
  databases {
    create("BudgetDatabase") {
      packageName = "actual.db"
      schemaOutputDirectory = file("src/commonMain/sqldelight/schemas")
      verifyMigrations = true
      verifyDefinitions = true
      generateAsync = true
      dialect(libs.sqldelight.dialect)
    }
  }
}

buildConfig {
  packageName("actual.db")
  className("DatabaseBuildConfig")
  useKotlinOutput { internalVisibility = true }

  buildConfigField(name = "FOREIGN_KEY_CONSTRAINTS", value = boolProperty(key = "actual.db.foreignKeyConstraints"))
}

commonMainDependencies {
  api(projects.budget.model)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.sqldelight.coroutines)
  implementation(libs.sqldelight.driver.sqlite)
  implementation(libs.sqldelight.primitive)
  implementation(libs.sqldelight.runtime)
  implementation(projects.core.model)
  implementation(projects.log)
}

commonTestDependencies {
  implementation(libs.test.alakazam.core)
  implementation(projects.core.files)
  implementation(projects.test.coroutines)
  implementation(projects.test.db)
}

androidMainDependencies {
  implementation(libs.sqldelight.driver.android)
}
