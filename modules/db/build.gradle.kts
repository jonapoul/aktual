import blueprint.core.androidMainDependencies
import blueprint.core.boolProperty
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

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
  generateAtSync = false
  buildConfigField(name = "FOREIGN_KEY_CONSTRAINTS", value = boolProperty(key = "actual.db.foreignKeyConstraints"))
}

kotlin {
  commonMainDependencies {
    api(projects.budget.model)
    implementation(libs.alakazam.db.sqldelight)
    implementation(libs.alakazam.kotlin.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
  }

  commonTestDependencies {
    implementation(libs.test.alakazam.core)
    implementation(libs.test.alakazam.sqldelight)
    implementation(projects.core.files)
    implementation(projects.test.coroutines)
    implementation(projects.test.db)
  }

  androidMainDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
