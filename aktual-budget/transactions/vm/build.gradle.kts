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
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:budgeting"))
    api(project(":aktual-budget:schedules"))
    api(project(":aktual-budget:tags"))
    api(project(":aktual-budget:transactions"))
    api(project(":aktual-prefs"))
    implementation(project(":aktual-budget:payees"))
    implementation(project(":aktual-budget:utils"))
  }

  commonTestDependencies {
    implementation(libs.androidx.paging.testing)
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-budget:banksync:impl"))
    implementation(project(":aktual-budget:budgeting:impl"))
    implementation(project(":aktual-budget:payees:impl"))
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:schedules:impl"))
    implementation(project(":aktual-budget:tags:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
    implementation(project(":aktual-di:runlevel"))
  }
}
