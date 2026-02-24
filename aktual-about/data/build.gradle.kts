import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.multiplatform")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.serialization.core)
    api(libs.ktor.core)
    compileOnly(libs.androidx.compose.annotation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-core:model"))
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
