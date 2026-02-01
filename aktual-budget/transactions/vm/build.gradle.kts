import blueprint.core.commonMainDependencies

plugins {
  alias(libs.plugins.module.viewmodel)
  alias(libs.plugins.convention.buildconfig)
}

buildConfig {
  packageName("aktual.budget.transactions.vm")
  useKotlinOutput { topLevelConstants = true }

  buildConfigField("PAGING_SIZE", expect<Int>())
  sourceSets {
    named("androidMain") { buildConfigField("PAGING_SIZE", 20) }
    named("jvmMain") { buildConfigField("PAGING_SIZE", 50) }
  }
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data"))
    api(project(":aktual-budget:model"))
    api(libs.alakazam.kotlin.core)
    api(libs.androidx.paging.common)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-core:di"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-prefs"))
  }

  androidHostTestDependencies {
    implementation(libs.sqldelight.driver.android)
    implementation(project(":aktual-app:di"))
  }
}
