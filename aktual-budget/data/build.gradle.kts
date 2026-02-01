import blueprint.core.androidMainDependencies
import blueprint.core.boolProperty
import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.wire)
  alias(libs.plugins.convention.buildconfig)
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

wire {
  kotlin {}
}

buildConfig {
  forClass("DatabaseBuildConfig") {
    packageName("aktual.budget.db")
    buildConfigField<Boolean>(
      "FOREIGN_KEY_CONSTRAINTS",
      providers.boolProperty(key = "aktual.db.foreignKeyConstraints"),
    )
  }

  sourceSets.named("test") {
    packageName("aktual.test")
    useKotlinOutput { topLevelConstants = true }
    buildConfigField<File>("RESOURCES_DIR", layout.projectDirectory.dir("src/commonTest/resources").asFile)
  }
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.datetime)
    api(libs.okio)
    api(libs.preferences.core)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.alakazam.db.sqldelight)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.driver.sqlite)
    implementation(libs.sqldelight.primitive)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-logging"))
  }

  commonTestDependencies {
    implementation(libs.test.alakazam.core)
    implementation(libs.test.alakazam.sqldelight)
  }

  androidMainDependencies {
    implementation(libs.sqldelight.driver.android)
  }
}
