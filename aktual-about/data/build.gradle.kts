plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(project(":aktual-core:utils"))
    api(project(":aktual-logging:format"))
    api(project(":github-api"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
  }

  commonTestDependencies {
    implementation(libs.ktor.test)
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
    implementation(project(":github-api:impl"))
  }
}
