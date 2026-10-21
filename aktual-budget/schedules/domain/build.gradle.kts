plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-budget:transactions:domain"))
    implementation(project(":aktual-core:logging"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
