plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:rules"))
    api(project(":aktual-budget:schedules"))
    implementation(project(":aktual-budget:transactions"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:schedules:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
  }
}
