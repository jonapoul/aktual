plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-budget:rules"))
    api(project(":aktual-budget:transactions:model"))
    compileOnly(libs.androidx.compose.annotation)
  }
}
