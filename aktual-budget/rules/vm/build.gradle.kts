plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:rules:api"))
    api(project(":aktual-budget:schedules:api"))
    implementation(project(":aktual-budget:transactions:api"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-budget:schedules:domain"))
    implementation(project(":aktual-budget:transactions:domain"))
  }
}
