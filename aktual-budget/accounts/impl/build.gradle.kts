plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:accounts"))
    api(project(":aktual-budget:transactions"))
    api(project(":aktual-budget:utils"))
    api(project(":aktual-core:utils"))
  }

  commonTestDependencies {
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-budget:rules:impl"))
    implementation(project(":aktual-budget:transactions:impl"))
    implementation(project(":aktual-test"))
  }
}
