plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
  alias(libs.plugins.kotlin.serialization)
}

buildConfig {
  packageName("aktual.budget.transactions.vm")
  useKotlinOutput { topLevelConstants = true }

  buildConfigField("PAGING_SIZE", expect<Int>())
  sourceSets {
    named("androidMain") { buildConfigField("PAGING_SIZE", 20) }
    named("desktopMain") { buildConfigField("PAGING_SIZE", 50) }
  }
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.androidx.paging.common)
    api(libs.compose.uiGraphics)
    api(libs.kotlinx.datetime)
    api(project(":aktual-budget:banksync:domain"))
    api(project(":aktual-budget:budgeting:domain"))
    api(project(":aktual-budget:tags:domain"))
    api(project(":aktual-prefs"))
  }

  androidHostTestDependencies {
    implementation(libs.androidx.paging.testing)
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:runlevel"))
    implementation(project(":aktual-test"))
  }
}
