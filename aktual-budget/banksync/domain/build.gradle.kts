plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:transactions:api"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:domain"))
    implementation(project(":aktual-budget:transactions:domain"))
    implementation(project(":aktual-test"))
  }
}
