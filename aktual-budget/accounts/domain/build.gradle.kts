plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:transactions:api"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-budget:transactions:domain"))
    implementation(project(":aktual-test"))
  }
}
