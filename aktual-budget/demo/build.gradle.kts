plugins { id("aktual.module.compose") }

compose.resources {
  generateResClass = always
  packageOfResClass = "aktual.budget.demo"
}

kotlin {
  android { androidResources.enable = true }

  commonMainDependencies {
    api(project(":aktual-budget"))
    api(project(":aktual-di:runlevel"))
    implementation(libs.compose.resources)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sqldelight.runtime)
    implementation(project(":aktual-budget:data:db"))
    implementation(project(":aktual-core"))
    implementation(project(":aktual-core:logging"))
  }
}
