plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-budget:data:db"))
    api(project(":aktual-core"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":aktual-core:logging"))
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
  }
}
