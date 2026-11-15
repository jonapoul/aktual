plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-api"))
    api(project(":aktual-budget:banksync"))
    api(project(":aktual-budget:transactions"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
    implementation(project(":aktual-test"))
  }
}
