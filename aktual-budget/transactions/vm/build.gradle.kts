import aktual.gradle.dsl.androidHostTestDependencies
import blueprint.core.commonMainDependencies

plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
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
    api(project(":aktual-budget:data:db"))
    api(libs.alakazam.kotlin)
    api(libs.androidx.paging.common)
    api(libs.kotlinx.datetime)
    implementation(project(":aktual-prefs"))
  }

  androidHostTestDependencies {
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-di:runlevel"))
    implementation(project(":aktual-test"))
  }
}
