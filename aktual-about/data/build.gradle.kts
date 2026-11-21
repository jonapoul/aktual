plugins {
  id("aktual.module.kotlin")
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(project(":aktual-about:model"))
    api(project(":aktual-core:utils"))
    api(project(":aktual-logging:format"))
    api(project(":github-api"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.core)
    implementation(libs.ktor.serialization.json)
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
    implementation(project(":github-api:impl"))
  }
}
