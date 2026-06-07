import aktual.gradle.dsl.androidHostTestDependencies
import aktual.gradle.dsl.desktopTestDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  id("aktual.convention.db-test")
}

kotlin {
  commonTestDependencies {
    // utils
    implementation(project(":aktual-app:di"))
    implementation(project(":aktual-budget:model"))
    implementation(project(":aktual-core:model"))
    implementation(project(":aktual-di:bindings"))
    implementation(project(":aktual-test"))

    // nav use cases needed by the root VMs
    implementation(project(":aktual-app:nav"))

    // the actual ViewModels
    implementation(project(":aktual-about:vm"))
    implementation(project(":aktual-account:vm"))
    implementation(project(":aktual-budget:list:vm"))
    implementation(project(":aktual-budget:reports:vm"))
    implementation(project(":aktual-budget:rules:vm"))
    implementation(project(":aktual-budget:schedules:vm"))
    implementation(project(":aktual-budget:sync:vm"))
    implementation(project(":aktual-budget:transactions:vm"))
    implementation(project(":aktual-metrics:vm"))
    implementation(project(":aktual-prefs:vm"))
  }

  desktopTestDependencies { implementation(project(":aktual-app:desktop")) }

  androidHostTestDependencies {
    implementation(project(":aktual-app:android"))
    implementation(libs.metrox.android)
  }
}
