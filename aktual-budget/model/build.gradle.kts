import blueprint.core.commonMainDependencies
import blueprint.core.commonTestDependencies

plugins {
  id("aktual.module.kotlin")
  id("aktual.compiler")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.immutable)
    api(libs.kotlinx.serialization.json)
    compileOnly(libs.androidx.compose.annotation)
  }

  commonTestDependencies {
    implementation(project(":aktual-test"))
    implementation(project(":aktual-test:api"))
  }
}
