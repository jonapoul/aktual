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
      packageName = "actual.budget.db"
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
  packageName("actual.budget.db")
  className("DatabaseBuildConfig")
  buildConfigField(name = "FOREIGN_KEY_CONSTRAINTS", value = boolProperty(key = "actual.db.foreignKeyConstraints"))
}

kotlin {
  commonMainDependencies {
    api(libs.preferences.core)
    api(project(":modules:budget:model"))
    api(project(":modules:core:model"))
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
  }

  androidMainDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
