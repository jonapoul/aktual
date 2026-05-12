import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    api(project(":aktual-core:model"))
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
