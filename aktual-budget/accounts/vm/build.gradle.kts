plugins {
  id("aktual.module.viewmodel")
  id("aktual.convention.db-test")
}

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:accounts"))
    implementation(libs.metrox.viewmodel)
    implementation(project(":aktual-budget:banksync"))
    implementation(project(":aktual-budget:transactions"))
    implementation(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:accounts:impl"))
    implementation(project(":aktual-budget:banksync:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
  }
}
