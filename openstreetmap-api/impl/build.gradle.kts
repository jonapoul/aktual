plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(project(":openstreetmap-api"))
    implementation(project(":aktual-di:core"))
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
